package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.Optional;

public record BlockTempData(List<Either<TagKey<Block>, Block>> blocks, double temperature, double range, double maxEffect, boolean fade,
                            BlockPredicate condition, Optional<CompoundTag> nbt, Optional<List<String>> requiredMods) implements IForgeRegistryEntry<BlockTempData>
{
    public static final Codec<BlockTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(TagKey.codec(Registry.BLOCK_REGISTRY), ForgeRegistries.BLOCKS.getCodec()).listOf().fieldOf("blocks").forGetter(BlockTempData::blocks),
            Codec.DOUBLE.fieldOf("temperature").forGetter(BlockTempData::temperature),
            Codec.DOUBLE.optionalFieldOf("max_effect", Double.MAX_VALUE).forGetter(BlockTempData::maxEffect),
            Codec.DOUBLE.optionalFieldOf("range", Double.MAX_VALUE).forGetter(BlockTempData::range),
            Codec.BOOL.optionalFieldOf("fade", true).forGetter(BlockTempData::fade),
            BlockPredicate.CODEC.optionalFieldOf("condition", BlockPredicate.alwaysTrue()).forGetter(BlockTempData::condition),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(BlockTempData::nbt),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(BlockTempData::requiredMods)
    ).apply(instance, BlockTempData::new));

    @Override
    public BlockTempData setRegistryName(ResourceLocation name)
    {
        return this;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return null;
    }

    @Override
    public Class<BlockTempData> getRegistryType()
    {
        return BlockTempData.class;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BlockTempData{blocks=[");
        for (Either<TagKey<Block>, Block> block : blocks)
        {
            if (block.left().isPresent())
            {   builder.append("#").append(block.left().get().toString());
            }
            else
            {   builder.append(block.right().get().toString());
            }
            builder.append(", ");
        }
        builder.append("], temperature=").append(temperature).append(", range=").append(range).append(", maxEffect=").append(maxEffect).append(", fade=").append(fade).append(", condition=").append(condition);
        nbt.ifPresent(tag -> builder.append(", nbt=").append(tag));
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append("}");
        return builder.toString();
    }
}
