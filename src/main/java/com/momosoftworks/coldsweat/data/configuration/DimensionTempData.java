package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record DimensionTempData(List<Either<TagKey<DimensionType>, ResourceLocation>> dimensions, double temperature, Temperature.Units units, boolean isOffset) implements IForgeRegistryEntry<DimensionTempData>
{
    public static final Codec<DimensionTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(
            // Convert from a string to a TagKey
            string ->
            {
                ResourceLocation tagLocation = ResourceLocation.tryParse(string.replace("#", ""));
                if (tagLocation == null) throw new IllegalArgumentException("Dimension tag is null");
                if (!string.contains("#")) return Either.<TagKey<DimensionType>, ResourceLocation>right(tagLocation);

                return Either.<TagKey<DimensionType>, ResourceLocation>left(TagKey.create(Registry.DIMENSION_TYPE_REGISTRY, tagLocation));
            },
            // Convert from a TagKey to a string
            tag ->
            {   if (tag == null) throw new IllegalArgumentException("Dimension tag is null");
                String result = tag.left().isPresent()
                                ? "#" + tag.left().get().location()
                                : tag.right().map(ResourceLocation::toString).orElse("");
                if (result.isEmpty()) throw new IllegalArgumentException("Dimension field is not a tag or valid ID");
                return result;
            })
            .listOf()
            .fieldOf("dimensions").forGetter(DimensionTempData::dimensions),
            Codec.DOUBLE.fieldOf("temperature").forGetter(DimensionTempData::temperature),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(DimensionTempData::units),
            Codec.BOOL.optionalFieldOf("is_offset", false).forGetter(DimensionTempData::isOffset)
    ).apply(instance, DimensionTempData::new));

    @Override
    public DimensionTempData setRegistryName(ResourceLocation name)
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
    public Class<DimensionTempData> getRegistryType()
    {
        return DimensionTempData.class;
    }
}