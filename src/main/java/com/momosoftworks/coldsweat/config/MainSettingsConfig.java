package com.momosoftworks.coldsweat.config;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainSettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    private static final MainSettingsConfig INSTANCE = new MainSettingsConfig();
    public  static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<Integer> difficulty;

    public static final ForgeConfigSpec.ConfigValue<Double> maxHabitable;
    public static final ForgeConfigSpec.ConfigValue<Double> minHabitable;
    public static final ForgeConfigSpec.ConfigValue<Double> rateMultiplier;
    public static final ForgeConfigSpec.ConfigValue<Double> tempDamage;

    public static final ForgeConfigSpec.ConfigValue<Boolean> fireResistanceEffect;
    public static final ForgeConfigSpec.ConfigValue<Boolean> iceResistanceEffect;

    public static final ForgeConfigSpec.ConfigValue<Boolean> damageScaling;
    public static final ForgeConfigSpec.ConfigValue<Boolean> requireThermometer;

    public static final ForgeConfigSpec.ConfigValue<Integer> gracePeriodLength;
    public static final ForgeConfigSpec.ConfigValue<Boolean> gracePeriodEnabled;

    public static final ForgeConfigSpec.ConfigValue<Boolean> heatstrokeFog;
    public static final ForgeConfigSpec.ConfigValue<Boolean> freezingHearts;
    public static final ForgeConfigSpec.ConfigValue<Boolean> coldKnockback;
    public static final ForgeConfigSpec.ConfigValue<Boolean> coldMining;
    public static final ForgeConfigSpec.ConfigValue<Boolean> coldMovement;

    static 
    {
        ConfigSettings.Difficulty defaultDiff = ConfigSettings.DEFAULT_DIFFICULTY;

        /*
         Details about how the player is affected by temperature
         */
        BUILDER.push("Difficulty");

        difficulty = BUILDER
                .comment("Overrides all other config options for easy difficulty management",
                         "This value is changed by the in-game config. It does nothing otherwise.")
                .defineInRange("Difficulty", defaultDiff.ordinal(), 0, ConfigSettings.Difficulty.values().length - 1);

        minHabitable = BUILDER
                .comment("Defines the minimum habitable temperature")
                .defineInRange("Minimum Habitable Temperature", defaultDiff.getOrDefault("min_temp", Temperature.convert(50, Temperature.Units.F, Temperature.Units.MC, true)),
                               Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        maxHabitable = BUILDER
                .comment("Defines the maximum habitable temperature")
                .defineInRange("Maximum Habitable Temperature", defaultDiff.getOrDefault("max_temp", Temperature.convert(100, Temperature.Units.F, Temperature.Units.MC, true)),
                               Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        rateMultiplier = BUILDER
                .comment("Rate at which the player's body temperature changes (default: 1.0 (100%))")
                .defineInRange("Rate Multiplier", defaultDiff.getOrDefault("temp_rate", 1d), 0d, Double.POSITIVE_INFINITY);

        tempDamage = BUILDER
                .comment("Damage dealt to the player when they are too hot or too cold")
                .defineInRange("Temperature Damage", defaultDiff.getOrDefault("temp_damage", 2d), 0d, Double.POSITIVE_INFINITY);

        damageScaling = BUILDER
                .comment("Sets whether damage scales with difficulty")
                .define("Damage Scaling", defaultDiff.getOrDefault("damage_scaling", true));

        BUILDER.pop();


        /*
         Potion effects affecting the player's temperature
         */
        BUILDER.push("Items");

        fireResistanceEffect = BUILDER
                .comment("Allow fire resistance to block overheating damage")
                .define("Fire Resistance Immunity", defaultDiff.getOrDefault("fire_resistance_enabled", true));

        iceResistanceEffect = BUILDER
                .comment("Allow ice resistance to block freezing damage")
                .define("Ice Resistance Immunity", defaultDiff.getOrDefault("ice_resistance_enabled", true));

        requireThermometer = BUILDER
            .comment("Thermometer item is required to see detailed world temperature")
            .define("Require Thermometer", defaultDiff.getOrDefault("require_thermometer", true));

        BUILDER.pop();


        /*
         Temperature effects
         */
        BUILDER.push("Temperature Effects");
            BUILDER.push("Hot");

            heatstrokeFog = BUILDER
                .comment("When set to true, the player's view distance will decrease when they are too hot")
                .define("Heatstroke Fog", defaultDiff.getOrDefault("heatstroke_fog", true));

            BUILDER.pop();

            BUILDER.push("Cold");

            freezingHearts = BUILDER
                .comment("When set to true, some of the player's hearts will freeze when they are too cold, preventing regeneration")
                .define("Freezing Hearts", defaultDiff.getOrDefault("freezing_hearts", true));

            coldKnockback = BUILDER
                .comment("When set to true, the player's attack knockback will be reduced when they are too cold")
                .define("Cold Knockback Reduction", defaultDiff.getOrDefault("knockback_impairment", true));

            coldMovement = BUILDER
                .comment("When set to true, the player's movement speed will be reduced when they are too cold")
                .define("Cold Slowness", defaultDiff.getOrDefault("cold_slowness", true));

            coldMining = BUILDER
                .comment("When set to true, the player's mining speed will be reduced when they are too cold")
                .define("Cold Mining Fatigue", defaultDiff.getOrDefault("cold_break_speed", true));

            BUILDER.pop();
        BUILDER.pop();


        BUILDER.push("Grace Period");

                gracePeriodLength = BUILDER
                .comment("The number of ticks after the player spawns during which they are immune to temperature effects")
                .defineInRange("Grace Period Length", defaultDiff.getOrDefault("grace_length", 6000), 0, Integer.MAX_VALUE);

                gracePeriodEnabled = BUILDER
                .comment("Enables the grace period")
                .define("Grace Period Enabled", defaultDiff.getOrDefault("grace_enabled", true));

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void setup()
    {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path csConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "coldsweat");

        // Create the config folder
        try
        {   Files.createDirectory(csConfigPath);
        }
        catch (Exception ignored) {}

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "coldsweat/main.toml");
    }

    public static MainSettingsConfig getInstance()
    {   return INSTANCE;
    }

    /*
     * Non-private values for use elsewhere
     */

    public int getDifficulty()
    {   return difficulty.get();
    }

    public boolean isFireResistanceEnabled()
    {   return fireResistanceEffect.get();
    }
    public boolean isIceResistanceEnabled()
    {   return iceResistanceEffect.get();
    }

    public boolean thermometerRequired()
    {   return requireThermometer.get();
    }

    public boolean doDamageScaling()
    {   return damageScaling.get();
    }

    public double getTempDamage()
    {   return tempDamage.get();
    }

    public double getMinTempHabitable()
    {   return minHabitable.get();
    }
    public double getMaxTempHabitable()
    {   return maxHabitable.get();
    }

    public double getRateMultiplier()
    {   return rateMultiplier.get();
    }

    public int getGracePeriodLength()
    {   return gracePeriodLength.get();
    }

    public boolean isGracePeriodEnabled()
    {   return gracePeriodEnabled.get();
    }

    public boolean heatstrokeFog()
    {   return heatstrokeFog.get();
    }

    public boolean freezingHearts()
    {   return freezingHearts.get();
    }
    public boolean coldKnockback()
    {   return coldKnockback.get();
    }
    public boolean coldMining()
    {   return coldMining.get();
    }
    public boolean coldMovement()
    {   return coldMovement.get();
    }

    /*
     * Safe set methods for config values
     */
    public synchronized void setDifficulty(int value)
    {   synchronized (difficulty)
        {   difficulty.set(value);
        }
    }

    public synchronized void setMaxHabitable(double temp)
    {   synchronized (maxHabitable)
        {   maxHabitable.set(temp);
        }
    }

    public synchronized void setMinHabitable(double temp)
    {   synchronized (minHabitable)
        {   minHabitable.set(temp);
        }
    }

    public synchronized void setRateMultiplier(double rate)
    {   synchronized (rateMultiplier)
        {   rateMultiplier.set(rate);
        }
    }

    public synchronized void setFireResistanceEnabled(boolean isEffective)
    {   synchronized (fireResistanceEffect)
        {   fireResistanceEffect.set(isEffective);
        }
    }

    public synchronized void setIceResistanceEnabled(boolean isEffective)
    {   synchronized (iceResistanceEffect)
        {   iceResistanceEffect.set(isEffective);
        }
    }

    public synchronized void setRequireThermometer(boolean required)
    {   synchronized (requireThermometer)
        {   requireThermometer.set(required);
        }
    }

    public synchronized void setDamageScaling(boolean enabled)
    {   synchronized (damageScaling)
        {   damageScaling.set(enabled);
        }
    }

    public synchronized void setTempDamage(double damage)
    {   synchronized (tempDamage)
        {   tempDamage.set(damage);
        }
    }

    public synchronized void setGracePeriodLength(int ticks)
    {   synchronized (gracePeriodLength)
        {   gracePeriodLength.set(ticks);
        }
    }

    public synchronized void setGracePeriodEnabled(boolean enabled)
    {   synchronized (gracePeriodEnabled)
        {   gracePeriodEnabled.set(enabled);
        }
    }

    public synchronized void setHeatstrokeFog(boolean fog)
    {   synchronized (heatstrokeFog)
        {   heatstrokeFog.set(fog);
        }
    }

    public synchronized void setFreezingHearts(boolean hearts)
    {   synchronized (freezingHearts)
        {   freezingHearts.set(hearts);
        }
    }

    public synchronized void setColdKnockback(boolean knockback)
    {   synchronized (coldKnockback)
        {   coldKnockback.set(knockback);
        }
    }

    public synchronized void setColdMining(boolean mining)
    {   synchronized (coldMining)
        {   coldMining.set(mining);
        }
    }

    public synchronized void setColdMovement(boolean movement)
    {   synchronized (coldMovement)
        {   coldMovement.set(movement);
        }
    }

    public void save()
    {   SPEC.save();
    }
}