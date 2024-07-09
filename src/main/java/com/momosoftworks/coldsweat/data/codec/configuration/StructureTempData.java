package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record StructureTempData(List<Either<TagKey<ConfiguredStructureFeature<?, ?>>, ConfiguredStructureFeature<?, ?>>> structures, double temperature,
                                Temperature.Units units, boolean isOffset, Optional<List<String>> requiredMods) implements IForgeRegistryEntry<StructureTempData>
{
    public static final Codec<StructureTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(TagKey.codec(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY), ConfiguredStructureFeature.DIRECT_CODEC).listOf().fieldOf("structures").forGetter(StructureTempData::structures),
            Codec.DOUBLE.fieldOf("temperature").forGetter(StructureTempData::temperature),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(StructureTempData::units),
            Codec.BOOL.optionalFieldOf("offset", false).forGetter(StructureTempData::isOffset),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(StructureTempData::requiredMods)
    ).apply(instance, StructureTempData::new));

    @Override
    public StructureTempData setRegistryName(ResourceLocation name)
    {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName()
    {
        return null;
    }

    @Override
    public Class<StructureTempData> getRegistryType()
    {
        return null;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("StructureTempData{structures=[");
        for (Either<TagKey<ConfiguredStructureFeature<?, ?>>, ConfiguredStructureFeature<?, ?>> structure : structures)
        {
            if (structure.left().isPresent())
            {   builder.append("#").append(structure.left().get().toString());
            }
            else
            {   builder.append(structure.right().get().toString());
            }
            builder.append(", ");
        }
        builder.append("], temperature=").append(temperature).append(", units=").append(units).append(", offset=").append(isOffset);
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append("}");
        return builder.toString();
    }
}