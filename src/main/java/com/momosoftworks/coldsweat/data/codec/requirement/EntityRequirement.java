package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public record EntityRequirement(Optional<EntityType<?>> type, Optional<LocationRequirement> location, Optional<LocationRequirement> standingOn,
                                Optional<EffectsRequirement> effects, Optional<NbtRequirement> nbt, Optional<EntityFlagsRequirement> flags,
                                Optional<EquipmentRequirement> equipment, Optional<PlayerDataRequirement> playerData,
                                Optional<EntityRequirement> vehicle, Optional<EntityRequirement> passenger, Optional<EntityRequirement> target)
{
    public static EntityRequirement ANY = new EntityRequirement(Optional.empty(), Optional.empty(), Optional.empty(),
                                                                Optional.empty(), Optional.empty(), Optional.empty(),
                                                                Optional.empty(), Optional.empty(), Optional.empty(),
                                                                Optional.empty(), Optional.empty());

    public static Codec<EntityRequirement> SIMPLE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ForgeRegistries.ENTITIES.getCodec().optionalFieldOf("type").forGetter(requirement -> requirement.type),
            LocationRequirement.CODEC.optionalFieldOf("location").forGetter(requirement -> requirement.location),
            LocationRequirement.CODEC.optionalFieldOf("standing_on").forGetter(requirement -> requirement.standingOn),
            EffectsRequirement.CODEC.optionalFieldOf("effects").forGetter(requirement -> requirement.effects),
            NbtRequirement.CODEC.optionalFieldOf("nbt").forGetter(requirement -> requirement.nbt),
            EntityFlagsRequirement.CODEC.optionalFieldOf("flags").forGetter(requirement -> requirement.flags),
            EquipmentRequirement.CODEC.optionalFieldOf("equipment").forGetter(requirement -> requirement.equipment)
    ).apply(instance, (type, location, standingOn, effects, nbt, flags, equipment) -> new EntityRequirement(type, location, standingOn, effects, nbt, flags, equipment,
                                                                                                            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())));

    private static final List<Codec<EntityRequirement>> REQUIREMENT_CODEC_STACK = new ArrayList<>(List.of(SIMPLE_CODEC));
    // Allow for up to 16 layers of inner codecs
    static
    {   for (int i = 0; i < 16; i++)
        {   getCodec();
        }
    }

    public static Codec<EntityRequirement> getCodec()
    {
        var latestCodec = REQUIREMENT_CODEC_STACK.get(REQUIREMENT_CODEC_STACK.size() - 1);
        var codec = RecordCodecBuilder.<EntityRequirement>create(instance -> instance.group(
                ForgeRegistries.ENTITIES.getCodec().optionalFieldOf("type").forGetter(requirement -> requirement.type),
                LocationRequirement.CODEC.optionalFieldOf("location").forGetter(requirement -> requirement.location),
                LocationRequirement.CODEC.optionalFieldOf("standing_on").forGetter(requirement -> requirement.standingOn),
                EffectsRequirement.CODEC.optionalFieldOf("effects").forGetter(requirement -> requirement.effects),
                NbtRequirement.CODEC.optionalFieldOf("nbt").forGetter(requirement -> requirement.nbt),
                EntityFlagsRequirement.CODEC.optionalFieldOf("flags").forGetter(requirement -> requirement.flags),
                EquipmentRequirement.CODEC.optionalFieldOf("equipment").forGetter(requirement -> requirement.equipment),
                PlayerDataRequirement.getCodec(latestCodec).optionalFieldOf("player_data").forGetter(requirement -> requirement.playerData),
                latestCodec.optionalFieldOf("vehicle").forGetter(requirement -> requirement.vehicle),
                latestCodec.optionalFieldOf("passenger").forGetter(requirement -> requirement.passenger),
                latestCodec.optionalFieldOf("target").forGetter(requirement -> requirement.target)
        ).apply(instance, EntityRequirement::new));

        REQUIREMENT_CODEC_STACK.add(codec);
        return codec;
    }

    public boolean test(Entity entity)
    {
        if (type.isPresent() && !type.get().equals(entity.getType()))
        {   return false;
        }
        if (location.isPresent() && !location.get().test(entity.level, entity.position()))
        {   return false;
        }
        if (standingOn.isPresent() && !standingOn.get().test(entity.level, entity.position()))
        {   return false;
        }
        if (effects.isPresent() && !effects.get().test(entity))
        {   return false;
        }
        if (nbt.isPresent() && !nbt.get().test(entity))
        {   return false;
        }
        if (flags.isPresent() && !flags.get().test(entity))
        {   return false;
        }
        if (equipment.isPresent() && !equipment.get().test(entity))
        {   return false;
        }
        if (playerData.isPresent() && !playerData.get().test(entity))
        {   return false;
        }
        if (vehicle.isPresent() && !vehicle.get().test(entity.getVehicle()))
        {   return false;
        }
        if (passenger.isPresent() && !passenger.get().test(entity.getPassengers().isEmpty() ? null : entity.getPassengers().get(0)))
        {   return false;
        }
        if (target.isPresent())
        {
            if (!(entity instanceof Monster))
            {   return false;
            }
            Monster monster = (Monster) entity;
            if (!target.get().test(monster.getTarget()))
            {   return false;
            }
        }
        return true;
    }

    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        type.ifPresent(type -> tag.putString("type", ForgeRegistries.ENTITIES.getKey(type).toString()));
        location.ifPresent(location -> tag.put("location", location.serialize()));
        standingOn.ifPresent(standingOn -> tag.put("standing_on", standingOn.serialize()));
        effects.ifPresent(effects -> tag.put("effects", effects.serialize()));
        nbt.ifPresent(nbt -> tag.put("nbt", nbt.serialize()));
        flags.ifPresent(flags -> tag.put("flags", flags.serialize()));
        equipment.ifPresent(equipment -> tag.put("equipment", equipment.serialize()));
        playerData.ifPresent(playerData -> tag.put("player_data", playerData.serialize()));
        vehicle.ifPresent(vehicle -> tag.put("vehicle", vehicle.serialize()));
        passenger.ifPresent(passenger -> tag.put("passenger", passenger.serialize()));
        target.ifPresent(target -> tag.put("target", target.serialize()));
        return tag;
    }

    public static EntityRequirement deserialize(CompoundTag tag)
    {
        Optional<EntityType<?>> type = tag.contains("type") ? Optional.of(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(tag.getString("type")))) : Optional.empty();
        Optional<LocationRequirement> location = tag.contains("location") ? Optional.of(LocationRequirement.deserialize(tag.getCompound("location"))) : Optional.empty();
        Optional<LocationRequirement> standingOn = tag.contains("standing_on") ? Optional.of(LocationRequirement.deserialize(tag.getCompound("standing_on"))) : Optional.empty();
        Optional<EffectsRequirement> effects = tag.contains("effects") ? Optional.of(EffectsRequirement.deserialize(tag.getCompound("effects"))) : Optional.empty();
        Optional<NbtRequirement> nbt = tag.contains("nbt") ? Optional.of(NbtRequirement.deserialize(tag.getCompound("nbt"))) : Optional.empty();
        Optional<EntityFlagsRequirement> flags = tag.contains("flags") ? Optional.of(EntityFlagsRequirement.deserialize(tag.getCompound("flags"))) : Optional.empty();
        Optional<EquipmentRequirement> equipment = tag.contains("equipment") ? Optional.of(EquipmentRequirement.deserialize(tag.getCompound("equipment"))) : Optional.empty();
        Optional<PlayerDataRequirement> playerData = tag.contains("player_data") ? Optional.of(PlayerDataRequirement.deserialize(tag.getCompound("player_data"))) : Optional.empty();
        Optional<EntityRequirement> vehicle = tag.contains("vehicle") ? Optional.of(EntityRequirement.deserialize(tag.getCompound("vehicle"))) : Optional.empty();
        Optional<EntityRequirement> passenger = tag.contains("passenger") ? Optional.of(EntityRequirement.deserialize(tag.getCompound("passenger"))) : Optional.empty();
        Optional<EntityRequirement> target = tag.contains("target") ? Optional.of(EntityRequirement.deserialize(tag.getCompound("target"))) : Optional.empty();
        return new EntityRequirement(type, location, standingOn, effects, nbt, flags, equipment, playerData, vehicle, passenger, target);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {   return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {   return false;
        }

        EntityRequirement that = (EntityRequirement) obj;

        return type.equals(that.type)
            && location.equals(that.location)
            && standingOn.equals(that.standingOn)
            && effects.equals(that.effects)
            && nbt.equals(that.nbt)
            && flags.equals(that.flags)
            && equipment.equals(that.equipment)
            && playerData.equals(that.playerData)
            && vehicle.equals(that.vehicle)
            && passenger.equals(that.passenger)
            && target.equals(that.target);
    }

    @Override
    public String toString()
    {
        return "Entity{" +
                "type=" + type +
                ", location=" + location +
                ", standingOn=" + standingOn +
                ", effects=" + effects +
                ", nbt=" + nbt +
                ", flags=" + flags +
                ", equipment=" + equipment +
                ", playerData=" + playerData +
                ", vehicle=" + vehicle +
                ", passenger=" + passenger +
                ", target=" + target +
                '}';
    }
}