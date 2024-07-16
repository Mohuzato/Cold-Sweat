package com.momosoftworks.coldsweat.common.entity.data.edible;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ColdBiomeEdible extends BiomeSearchingEdible
{
    public ColdBiomeEdible()
    {
        super((level, biome) ->
        {
            Pair<Double, Double> minMaxTemp = WorldHelper.getBiomeTemperatureRange(level, biome.value());
            double biomeTemp = CSMath.averagePair(minMaxTemp);

            return biomeTemp <= 0.2;
        });
    }

    @Override
    public int getCooldown()
    {   return (int) (Math.random() * 400 + 1200);
    }

    @Override
    public TagKey<Item> associatedItems()
    {   return ModItemTags.CHAMELEON_COLD;
    }
}
