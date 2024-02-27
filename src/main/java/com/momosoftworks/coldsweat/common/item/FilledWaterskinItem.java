package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.api.temperature.modifier.WaterskinTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.event.TooltipHandler;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.init.ItemInit;
import com.momosoftworks.coldsweat.core.itemgroup.ColdSweatGroup;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.ParticleBatchMessage;
import com.momosoftworks.coldsweat.core.network.message.UseFilledWaterskinMessage;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.UseAction;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
public class FilledWaterskinItem extends Item
{
    public static final double EFFECT_RATE = 0.4;
    public static final String NBT_TEMPERATURE = "Temperature";

    public FilledWaterskinItem()
    {
        super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1).craftRemainder(ItemInit.WATERSKIN.get()));

        DispenserBlock.registerBehavior(this, (source, stack) ->
        {
            BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            World level = source.getLevel();
            IChunk chunk = WorldHelper.getChunk(level, pos);
            double itemTemp = stack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE);

            if (chunk == null) return stack;

            // Play sound
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT,
                                 SoundCategory.PLAYERS, 1, (float) ((Math.random() / 5) + 0.9), false);

            // Spawn particles
            Random rand = new Random();
            for (int i = 0; i < 6; i++)
            {
                TaskScheduler.scheduleServer(() ->
                {
                    ParticleBatchMessage particles = new ParticleBatchMessage();
                    for (int p = 0; p < rand.nextInt(5) + 5; p++)
                    {
                        particles.addParticle(ParticleTypes.FALLING_WATER,
                                new ParticleBatchMessage.ParticlePlacement(pos.getX() + rand.nextDouble(),
                                                                           pos.getY() + rand.nextDouble(),
                                                                           pos.getZ() + rand.nextDouble(), 0, 0, 0));
                    }
                    ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> (Chunk) chunk), particles);
                }, i);
            }

            // Spawn a hitbox that falls at the same rate as the particles and gives players below the waterskin effect
            new Object()
            {
                double acceleration = 0;
                int tick = 0;
                AxisAlignedBB aabb = new AxisAlignedBB(pos).inflate(0.5);
                // Track affected players to prevent duplicate effects
                List<PlayerEntity> affectedPlayers = new ArrayList<>();

                void start()
                {   MinecraftForge.EVENT_BUS.register(this);
                }

                @SubscribeEvent
                public void onTick(TickEvent.WorldTickEvent event)
                {
                    if (event.world.isClientSide == level.isClientSide && event.phase == TickEvent.Phase.START)
                    {
                        // Temperature of waterskin weakens over time
                        double waterTemp = CSMath.blend(itemTemp, itemTemp / 5, tick, 20, 100);

                        // Move the box down at the speed of gravity
                        aabb = aabb.move(0, -acceleration, 0);

                        // If there's ground, stop
                        BlockPos pos = new BlockPos(aabb.minX, aabb.minY, aabb.minZ);
                        if (WorldHelper.isSpreadBlocked(level, chunk.getBlockState(pos), pos, Direction.DOWN, Direction.DOWN))
                        {   MinecraftForge.EVENT_BUS.unregister(this);
                            return;
                        }

                        // Apply the waterskin modifier to all entities in the box
                        level.getEntitiesOfClass(PlayerEntity.class, aabb).forEach(player ->
                        {
                            if (!affectedPlayers.contains(player))
                            {   // Apply the effect and store the player
                                Temperature.addModifier(player, new WaterskinTempModifier(waterTemp).expires(0), Temperature.Type.CORE, true);
                                affectedPlayers.add(player);
                            }
                        });

                        // Increase the speed of the box
                        acceleration += 0.0052;
                        tick++;

                        // Expire after 5 seconds
                        if (tick > 100)
                        {
                            MinecraftForge.EVENT_BUS.unregister(this);
                        }
                    }
                }
            }.start();

            return getEmpty(stack);
        });
    }

    @Override
    public void inventoryTick(ItemStack itemstack, World world, Entity entity, int slot, boolean isSelected)
    {
        super.inventoryTick(itemstack, world, entity, slot, isSelected);
        if (entity.tickCount % 5 == 0 && entity instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) entity;
            double itemTemp = itemstack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE);
            if (itemTemp != 0 && slot <= 8 || player.getOffhandItem().equals(itemstack))
            {
                double temp = (EFFECT_RATE / 20) * ConfigSettings.TEMP_RATE.get();
                double newTemp = CSMath.shrink(itemTemp, temp * 5);

                itemstack.getOrCreateTag().putDouble(FilledWaterskinItem.NBT_TEMPERATURE, newTemp);
                Temperature.addModifier(player, new WaterskinTempModifier(temp * CSMath.sign(itemTemp)).expires(5), Temperature.Type.CORE, true);
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClick(PlayerInteractEvent.LeftClickEmpty event)
    {
        if (event.getItemStack().getItem() instanceof FilledWaterskinItem)
        {   ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new UseFilledWaterskinMessage());
            performPourAction(event.getItemStack(), event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        if (event.getItemStack().getItem() instanceof FilledWaterskinItem)
        {   performPourAction(event.getItemStack(), event.getEntityLiving());
            event.setCanceled(true);
        }
    }

    @Override
    public boolean canAttackBlock(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer)
    {   return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity)
    {   return performPourAction(stack, player);
    }

    public static boolean performPourAction(ItemStack stack, LivingEntity entity)
    {
        if (!(entity instanceof PlayerEntity && stack.getItem() == ModItems.FILLED_WATERSKIN)) return false;

        PlayerEntity player = ((PlayerEntity) entity);
        World level = player.level;
        double amount = stack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE) * (ConfigSettings.WATERSKIN_STRENGTH.get() / 50d);
        Temperature.addModifier(player, new WaterskinTempModifier(amount).expires(0), Temperature.Type.CORE, true);

        // Play empty sound
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT,
                             SoundCategory.PLAYERS, 1, (float) ((Math.random() / 5) + 0.9), false);

        // Create empty waterskin item
        ItemStack emptyStack = getEmpty(stack);
        emptyStack.getOrCreateTag().remove("Purity");

        // Add the item to the player's inventory
        if (player.inventory.contains(emptyStack))
        {   player.addItem(emptyStack);
            player.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        }
        else
        {   player.setItemInHand(Hand.MAIN_HAND, emptyStack);
        }

        player.swing(Hand.MAIN_HAND);

        // spawn falling water particles
        Random rand = new Random();
        for (int i = 0; i < 6; i++)
        {
            TaskScheduler.scheduleClient(() ->
            {
                for (int p = 0; p < rand.nextInt(5) + 5; p++)
                {
                    level.addParticle(ParticleTypes.FALLING_WATER,
                                      player.getX() + rand.nextFloat() * player.getBbWidth() - (player.getBbWidth() / 2),
                                      player.getY() + player.getBbHeight() + rand.nextFloat() * 0.5,
                                      player.getZ() + rand.nextFloat() * player.getBbWidth() - (player.getBbWidth() / 2), 0.3, 0.3, 0.3);
                }
            }, i);
        }
        player.clearFire();
        player.getCooldowns().addCooldown(ModItems.FILLED_WATERSKIN, 10);
        player.getCooldowns().addCooldown(ModItems.WATERSKIN, 10);

        return true;
    }

    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand)
    {   return DrinkHelper.useDrink(level, player, hand);
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack)
    {   return UseAction.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {   return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World level, LivingEntity entity)
    {   double amount = stack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE) * (ConfigSettings.WATERSKIN_STRENGTH.get() / 50d);
        Temperature.addModifier(entity, new WaterskinTempModifier(amount / 100).expires(100), Temperature.Type.CORE, true);
        return entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()
               ? stack
               : this.getContainerItem(stack);
    }

    public static ItemStack getEmpty(ItemStack stack)
    {
        if (stack.getItem() instanceof FilledWaterskinItem)
        {
            ItemStack emptyWaterskin = new ItemStack(ModItems.WATERSKIN);

            // Preserve NBT (except temperature)
            emptyWaterskin.setTag(stack.getTag());
            emptyWaterskin.removeTagKey(FilledWaterskinItem.NBT_TEMPERATURE);
            return emptyWaterskin;
        }
        return stack;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, World level, List<ITextComponent> tooltip, ITooltipFlag advanced)
    {
        double temp = stack.getOrCreateTag().getDouble(FilledWaterskinItem.NBT_TEMPERATURE);
        // Info tooltip for hotbar functionality
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new TranslationTextComponent("tooltip.cold_sweat.hotbar").withStyle(TextFormatting.GRAY));
        tooltip.add(new TranslationTextComponent("tooltip.cold_sweat.temperature_effect",
                                           (CSMath.sign(temp) >= 0 ? "+" : "-")
                                           + (temp != 0 ? CSMath.truncate(EFFECT_RATE * ConfigSettings.TEMP_RATE.get(), 2) : 0))
                            .withStyle(temp > 0 ? TooltipHandler.HOT : temp < 0 ? TooltipHandler.COLD : TextFormatting.WHITE));

        // Tooltip to display temperature
        boolean celsius = ConfigSettings.CELSIUS.get();
        TextFormatting color = temp == 0 ? TextFormatting.GRAY : (temp < 0 ? TextFormatting.BLUE : TextFormatting.RED);
        String tempUnits = celsius ? "C" : "F";
        temp = temp / 2 + 95;
        if (celsius) temp = Temperature.convertUnits(temp, Temperature.Units.F, Temperature.Units.C, true);
        temp += ConfigSettings.TEMP_OFFSET.get() / 2.0;

        tooltip.add(1, new TranslationTextComponent("item.cold_sweat.waterskin.filled").withStyle(TextFormatting.GRAY)
                       .append(" (")
                       .append(new StringTextComponent((int) temp + " \u00B0" + tempUnits).withStyle(color))
                       .append(new StringTextComponent(")").withStyle(TextFormatting.GRAY)));

        super.appendHoverText(stack, level, tooltip, advanced);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {   return slotChanged;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack)
    {   return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack)
    {   ItemStack empty = getEmpty(itemStack);
        empty.getOrCreateTag().remove("Purity");
        return empty;
    }

    public String getDescriptionId()
    {   return new TranslationTextComponent("item.cold_sweat.waterskin").getString();
    }
}
