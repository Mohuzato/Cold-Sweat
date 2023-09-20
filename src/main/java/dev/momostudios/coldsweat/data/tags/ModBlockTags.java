package dev.momostudios.coldsweat.data.tags;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModBlockTags
{
    public static final TagKey<Block> CAMPFIRES = createTag("campfires");
    public static final TagKey<Block> SOUL_CAMPFIRES = createTag("soul_campfires");
    public static final TagKey<Block> SOUL_STALK_PLACEABLE_ON = createTag("may_place_on/soul_stalk");
    public static final TagKey<Block> SOUL_SAND_REPLACEABLE = createTag("soul_sand_replaceable");

    private static TagKey<Block> createTag(String name)
    {
        return BlockTags.create(new ResourceLocation(ColdSweat.MOD_ID, name));
    }
}
