package com.momosoftworks.coldsweat.data;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ModRegistries
{
    // Item Registries
    public static final ResourceKey<Registry<InsulatorData>> INSULATOR_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "item/insulator"));
    public static final ResourceKey<Registry<FuelData>> FUEL_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "item/fuel"));
    public static final ResourceKey<Registry<FoodData>> FOOD_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "item/food"));
    public static final ResourceKey<Registry<ItemCarryTempData>> CARRY_TEMP_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "item/carried_temp"));

    // World Registries
    public static final ResourceKey<Registry<BlockTempData>> BLOCK_TEMP_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "block/block_temp"));
    public static final ResourceKey<Registry<BiomeTempData>> BIOME_TEMP_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "world/biome_temp"));
    public static final ResourceKey<Registry<DimensionTempData>> DIMENSION_TEMP_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "world/dimension_temp"));
    public static final ResourceKey<Registry<StructureTempData>> STRUCTURE_TEMP_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "world/structure_temp"));
    public static final ResourceKey<Registry<DepthTempData>> DEPTH_TEMP_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "world/temp_region"));

    // Entity Registries
    public static final ResourceKey<Registry<MountData>> MOUNT_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "entity/mount"));
    public static final ResourceKey<Registry<SpawnBiomeData>> ENTITY_SPAWN_BIOME_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "entity/spawn_biome"));
    public static final ResourceKey<Registry<EntityTempData>> ENTITY_TEMP_DATA = ResourceKey.createRegistryKey(new ResourceLocation(ColdSweat.MOD_ID, "entity/entity_temp"));
}
