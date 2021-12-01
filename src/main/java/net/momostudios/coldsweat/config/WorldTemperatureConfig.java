package net.momostudios.coldsweat.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class WorldTemperatureConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<List<? extends List<String>>> biomeOffsets;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<String>>> biomeTemperatures;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<String>>> dimensionOffsets;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<String>>> dimensionTemperatures;

    public static final WorldTemperatureConfig INSTANCE = new WorldTemperatureConfig();

    static
    {
        /*
         Dimensions
         */
        BUILDER.comment("Notation: [[\"dimension1\", \"temperature1\"], [\"dimension2\", \"temperature2\"]... etc]",
            "Common dimension IDs: minecraft:overworld, minecraft:the_nether, minecraft:the_end",
            "Note: all temperatures are in Minecraft units")
            .push("Dimensions");

        BUILDER.push("DimensionTemperatureOffset");
        dimensionOffsets = BUILDER
            .defineList("Dimension Temperature Offsets", Arrays.asList(
                    Arrays.asList("minecraft:the_nether", "0.6"),
                    Arrays.asList("minecraft:the_end", "-0.2")
            ), it -> ((List) it).get(0) instanceof String && ((List) it).get(1) instanceof String);
        BUILDER.pop();

        BUILDER.push("DimensionTemperatures");
        dimensionTemperatures = BUILDER
            .comment("Override their respective offset values",
                "Also override ALL biome temperatures")
            .defineList("Dimension Temperatures", Arrays.asList(
                    // No default values
            ), it -> ((List) it).get(0) instanceof String && ((List) it).get(1) instanceof String);
        BUILDER.pop();

        BUILDER.pop();

        /*
         Biomes
         */
        BUILDER.comment("Notation: [[\"biome1\", \"temperature1\"], [\"biome2\", \"temperature2\"]... etc]",
            "Note: all temperatures are in Minecraft units")
        .push("Biomes");

        BUILDER.push("BiomeTemperatureOffsets");
        biomeOffsets = BUILDER
            .defineList("Biome Temperature Offsets", Arrays.asList(
                    Arrays.asList("minecraft:soul_sand_valley", "-0.5")
            ), it -> ((List) it).get(0) instanceof String && ((List) it).get(1) instanceof String);
        BUILDER.pop();

        BUILDER.push("BiomeTemperatures");
        biomeTemperatures = BUILDER
            .comment("Temperatures for individual biomes")
            .defineList("Biome Temperatures", Arrays.asList(
                    // No default values
            ), it -> ((List) it).get(0) instanceof String && ((List) it).get(1) instanceof String);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void setup()
    {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path csConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "coldsweat");

        // Create the config folder
        try
        {
            Files.createDirectory(csConfigPath);
        }
        catch (Exception e)
        {
            // Do nothing
        }

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, SPEC, "coldsweat/world_temperatures.toml");
    }

    /*
     * Non-private values for use elsewhere
     */
    public List<? extends List<String>> biomeOffsets() {
        return biomeOffsets.get();
    }
    public List<? extends List<String>> biomeTemperatures() {
        return biomeTemperatures.get();
    }

    public List<? extends List<String>> dimensionOffsets() {
        return dimensionOffsets.get();
    }
    public List<? extends List<String>> dimensionTemperatures() {
        return dimensionTemperatures.get();
    }
}
