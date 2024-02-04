package com.momosoftworks.coldsweat.common.blockentity;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.modifier.HearthTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.block.IceboxBlock;
import com.momosoftworks.coldsweat.common.capability.EntityTempManager;
import com.momosoftworks.coldsweat.common.container.IceboxContainer;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.init.ParticleTypesInit;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.BlockDataUpdateMessage;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModBlockEntities;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class IceboxBlockEntity extends HearthBlockEntity implements ITickableTileEntity, ISidedInventory
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};

    LazyOptional<? extends IItemHandler>[] slotHandlers =
            SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

    List<ServerPlayerEntity> usingPlayers = new ArrayList<>();

    public IceboxBlockEntity()
    {   super();
        TaskScheduler.schedule(this::checkForSmokestack, 5);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag()
    {   CompoundNBT tag = super.getUpdateTag();
        tag.putInt("Fuel", this.getFuel());
        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag)
    {   this.setFuel(tag.getInt("Fuel"));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {   handleUpdateTag(null, pkt.getTag());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {   return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    private void sendUpdatePacket()
    {
        // Remove the players that aren't interacting with this block anymore
        usingPlayers.removeIf(player -> !(player.containerMenu instanceof IceboxContainer && ((IceboxContainer) player.containerMenu).te == this));

        // Send data to all players with this block's menu open
        ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.NMLIST.with(()-> usingPlayers.stream().map(player -> player.connection.connection).collect(Collectors.toList())),
                                             new BlockDataUpdateMessage(this));
    }

    @Override
    protected ITextComponent getDefaultName()
    {   return new TranslationTextComponent("container." + ColdSweat.MOD_ID + ".icebox");
    }

    @Override
    public ITextComponent getDisplayName()
    {   return this.getCustomName() != null ? this.getCustomName() : this.getDefaultName();
    }

    @Override
    public void tick()
    {
        super.tick();

        BlockState state = this.level.getBlockState(this.worldPosition);

        if (getFuel() > 0)
        {
            // Set state to frosted
            if (!state.getValue(IceboxBlock.FROSTED))
                level.setBlock(this.worldPosition, state.setValue(IceboxBlock.FROSTED, true), 3);

            // Cool down waterskins
            if (ticksExisted % 20 == 0)
            {
                boolean hasItemStacks = false;
                for (int i = 1; i < 10; i++)
                {
                    ItemStack stack = getItem(i);
                    int itemTemp = stack.getOrCreateTag().getInt("temperature");

                    if (stack.getItem() == ModItems.FILLED_WATERSKIN && itemTemp > -50)
                    {
                        hasItemStacks = true;
                        stack.getOrCreateTag().putInt("temperature", itemTemp - 1);
                    }
                }
                if (hasItemStacks) setFuel(getFuel() - 1);
            }
        }
        // if no fuel, set state to unfrosted
        else if (state.getValue(IceboxBlock.FROSTED))
        {   level.setBlock(this.worldPosition, state.setValue(IceboxBlock.FROSTED, false), 3);
        }
    }

    @Override
    public int getMaxPaths()
    {   return 1500;
    }

    @Override
    public int getSpreadRange()
    {   return 16;
    }

    @Override
    public int getMaxInsulationLevel()
    {   return 5;
    }

    @Override
    public boolean hasSmokeStack()
    {   return this.hasSmokestack;
    }

    @Override
    protected void trySpreading(int pathCount, int firstIndex, int lastIndex)
    {
        if (this.hasSmokestack)
        {   super.trySpreading(pathCount, firstIndex, lastIndex);
        }
    }

    @Override
    void insulatePlayer(PlayerEntity player)
    {
        // Apply the insulation effect
        if (!shouldUseColdFuel)
        EntityTempManager.getTemperatureCap(player).ifPresent(cap ->
        {   double temp = cap.getTemp(Temperature.Type.WORLD);
            double min = ConfigSettings.MIN_TEMP.get() + cap.getTemp(Temperature.Type.BURNING_POINT);
            double max = ConfigSettings.MAX_TEMP.get() + cap.getTemp(Temperature.Type.FREEZING_POINT);

            // If the player is habitable, check the input temperature reported by their HearthTempModifier (if they have one)
            if (CSMath.isWithin(temp, min, max))
            {
                // Find the player's HearthTempModifier
                TempModifier modifier = null;
                for (TempModifier tempModifier : cap.getModifiers(Temperature.Type.WORLD))
                {   if (tempModifier instanceof HearthTempModifier)
                {   modifier = tempModifier;
                    break;
                }
                }
                // If they have one, refresh it
                if (modifier != null)
                {   if (modifier.getExpireTime() - modifier.getTicksExisted() > 20)
                {   return;
                }
                    temp = modifier.getLastInput();
                }
                // This means the player is not insulated, and they are habitable without it
                else return;
            }

            // Tell the hearth to use hot fuel
            shouldUseColdFuel |= this.getColdFuel() > 0 && temp > max;
        });
        if (shouldUseHotFuel)
        {   int maxEffect = this.getMaxInsulationLevel() - 1;
            int effectLevel = (int) Math.min(maxEffect, (insulationLevel / (double) this.getInsulationTime()) * maxEffect);
            player.addEffect(new EffectInstance(ModEffects.INSULATION, 120, effectLevel, false, false, true));
        }
    }

    @Override
    public int getItemFuel(ItemStack item)
    {   return ConfigSettings.ICEBOX_FUEL.get().getOrDefault(item.getItem(), 0d).intValue();
    }

    @Override
    protected void drainFuel()
    {
        ItemStack fuelStack = this.getItem(0);
        int itemFuel = getItemFuel(fuelStack);

        if (itemFuel != 0 && this.getFuel() < this.getMaxFuel() - itemFuel / 2)
        {
            if (fuelStack.hasContainerItem() && fuelStack.getCount() == 1)
            {   this.setItem(0, fuelStack.getContainerItem());
                this.setFuel(this.getFuel() + itemFuel);
            }
            else
            {   int consumeCount = Math.min((int) Math.floor((this.getMaxFuel() - this.getFuel()) / (double) Math.abs(itemFuel)), fuelStack.getCount());
                fuelStack.shrink(consumeCount);
                this.setFuel(this.getFuel() + itemFuel * consumeCount);
            }
        }
    }

    public int getFuel()
    {   return this.getColdFuel();
    }

    public void setFuel(int amount)
    {   this.setColdFuel(amount, true);
    }

    @Override
    public void setColdFuel(int amount, boolean update)
    {   super.setColdFuel(amount, update);
        this.sendUpdatePacket();
    }

    @Override
    protected Container createMenu(int id, PlayerInventory playerInv)
    {   // Track the players using this block
        if (playerInv.player instanceof ServerPlayerEntity)
        {   usingPlayers.add((ServerPlayerEntity) playerInv.player);
        }
        return new IceboxContainer(id, playerInv, this);
    }

    @Override
    protected void tickParticles()
    {
        if (this.hasSmokestack)
        {   super.tickParticles();
        }
    }

    @Override
    public BasicParticleType getAirParticle()
    {   return ParticleTypesInit.GROUND_MIST.get();
    }

    @Override
    public void spawnAirParticle(int x, int y, int z, Random rand)
    {
        BlockPos pos = new BlockPos(x, y, z);
        boolean onGround = !this.level.getBlockState(pos.below()).isAir();
        if (rand.nextFloat() > (spreading ? 0.016f : 0.032f))
        {   return;
        }

        float xr = rand.nextFloat();
        float yr = onGround ? 0.1f : rand.nextFloat();
        float zr = rand.nextFloat();
        float xm = rand.nextFloat() / 20 - 0.025f;
        float zm = rand.nextFloat() / 20 - 0.025f;

        level.addParticle(onGround ? ParticleTypesInit.GROUND_MIST.get()
                                   : ParticleTypesInit.MIST.get(), false, x + xr, y + yr, z + zr, xm, 0, zm);
    }

    @Override
    public void load(BlockState state, CompoundNBT tag)
    {   super.load(state, tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, this.items);
        this.setFuel(tag.getInt("Fuel"));
    }

    @Override
    public CompoundNBT save(CompoundNBT tag)
    {   CompoundNBT saved = super.save(tag);
        ItemStackHelper.saveAllItems(saved, this.items);
        saved.putInt("Fuel", this.getFuel());

        return saved;
    }

    @Override
    public int getContainerSize()
    {   return 10;
    }

    @Override
    public ItemStack removeItem(int slot, int count)
    {   ItemStack itemstack = ItemStackHelper.removeItem(items, slot, count);
        if (!itemstack.isEmpty())
        {   this.setChanged();
        }
        return itemstack;
    }

    @Override
    public int[] getSlotsForFace(Direction dir)
    {   return dir.getAxis() == Direction.Axis.Y ? WATERSKIN_SLOTS : FUEL_SLOT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction)
    {
        if (slot == 0)
            return this.getItemFuel(stack) != 0;
        else return stack.getItem() == ModItems.WATERSKIN || stack.getItem() == ModItems.FILLED_WATERSKIN;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction)
    {   return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
    {
        if (!this.remove && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            if (facing == Direction.UP)
                return slotHandlers[0].cast();
            else if (facing == Direction.DOWN)
                return slotHandlers[1].cast();
            else
                return slotHandlers[2].cast();
        }
        return super.getCapability(capability, facing);
    }
}
