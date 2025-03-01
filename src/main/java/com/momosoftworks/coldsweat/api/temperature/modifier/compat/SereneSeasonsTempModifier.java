package com.momosoftworks.coldsweat.api.temperature.modifier.compat;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.LivingEntity;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.ServerConfig;

import java.util.function.Function;

/**
 * Special TempModifier class for Serene Seasons
 */
public class SereneSeasonsTempModifier extends TempModifier
{
    public SereneSeasonsTempModifier() {}

    @Override
    public Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait)
    {
        if (ServerConfig.whitelistedDimensions.get().contains(entity.level().dimension().location().toString()))
        {
            ISeasonState season = SeasonHelper.getSeasonState(entity.level());
            double startValue;
            double endValue;

            switch (season.getSubSeason())
            {
                case EARLY_AUTUMN -> { startValue = ConfigSettings.AUTUMN_TEMPS.get()[0]; endValue = ConfigSettings.AUTUMN_TEMPS.get()[1]; }
                case MID_AUTUMN   -> { startValue = ConfigSettings.AUTUMN_TEMPS.get()[1]; endValue = ConfigSettings.AUTUMN_TEMPS.get()[2]; }
                case LATE_AUTUMN  -> { startValue = ConfigSettings.AUTUMN_TEMPS.get()[2]; endValue = ConfigSettings.WINTER_TEMPS.get()[0]; }

                case EARLY_WINTER -> { startValue = ConfigSettings.WINTER_TEMPS.get()[0]; endValue = ConfigSettings.WINTER_TEMPS.get()[1]; }
                case MID_WINTER   -> { startValue = ConfigSettings.WINTER_TEMPS.get()[1]; endValue = ConfigSettings.WINTER_TEMPS.get()[2]; }
                case LATE_WINTER  -> { startValue = ConfigSettings.WINTER_TEMPS.get()[2]; endValue = ConfigSettings.SPRING_TEMPS.get()[0]; }

                case EARLY_SPRING -> { startValue = ConfigSettings.SPRING_TEMPS.get()[0]; endValue = ConfigSettings.SPRING_TEMPS.get()[1]; }
                case MID_SPRING   -> { startValue = ConfigSettings.SPRING_TEMPS.get()[1]; endValue = ConfigSettings.SPRING_TEMPS.get()[2]; }
                case LATE_SPRING  -> { startValue = ConfigSettings.SPRING_TEMPS.get()[2]; endValue = ConfigSettings.SUMMER_TEMPS.get()[0]; }

                case EARLY_SUMMER -> { startValue = ConfigSettings.SUMMER_TEMPS.get()[0]; endValue = ConfigSettings.SUMMER_TEMPS.get()[1]; }
                case MID_SUMMER   -> { startValue = ConfigSettings.SUMMER_TEMPS.get()[1]; endValue = ConfigSettings.SUMMER_TEMPS.get()[2]; }
                case LATE_SUMMER  -> { startValue = ConfigSettings.SUMMER_TEMPS.get()[2]; endValue = ConfigSettings.AUTUMN_TEMPS.get()[0]; }

                default -> { return temp -> temp; }

            }
            return temp -> temp + (float) CSMath.blend(startValue, endValue, season.getDay() % (season.getSubSeasonDuration() / season.getDayDuration()), 0, 8);
        }

        return temp -> temp;
    }
}
