package com.momosoftworks.coldsweat.api.event.common;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;

import java.util.function.Predicate;

/**
 * These events are fired when dealing with {@link TempModifier}s. <br>
 * They should not be side-specific. Do not limit them to run on any one side as it will cause desyncs.
 */
public class TempModifierEvent extends Event
{
    /**
     * Fired when a {@link TempModifier} is about to be added to an entity. <br>
     * <br>
     * {@link #entity} is the player the TempModifier is being applied to. <br>
     * {@link #trait} determines the modifier's {@link Temperature.Trait}. It will never be {@link Temperature.Trait#BODY} <br>
     * <br>
     * This event is {@link net.minecraftforge.eventbus.api.Cancelable}. <br>
     * Canceling this event will prevent the TempModifier from being added.<br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    @Cancelable
    public static class Add extends TempModifierEvent
    {
        private final LivingEntity entity;
        private TempModifier modifier;
        public Temperature.Trait trait;

        public void setModifierType(Temperature.Trait newTrait) {
            this.trait = newTrait;
        }

        public final TempModifier getModifier() {
            return modifier;
        }

        public void setModifier(TempModifier modifier) {
            this.modifier = modifier;
        }

        public final LivingEntity getEntity() {
            return entity;
        }

        public Add(TempModifier modifier, LivingEntity entity, Temperature.Trait trait)
        {
            this.entity = entity;
            this.trait = trait;
            this.modifier = modifier;
        }
    }


    /**
     * Fired when a {@link TempModifier} is about to be removed from an entity. <br>
     * <br>
     * {@link #entity} is the player the TempModifier is being removed from. <br>
     * {@link #trait} is the modifier's {@link Temperature.Trait}. It will never be {@link Temperature.Trait#BODY}. <br>
     * {@link #count} is the number of TempModifiers of the specified class being removed. <br>
     * {@link #condition} is the predicate used to determine which TempModifiers are being removed. <br>
     * <br>
     * This event is {@link net.minecraftforge.eventbus.api.Cancelable}. <br>
     * Canceling this event will prevent the TempModifier from being removed. <br>
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     */
    @Cancelable
    public static class Remove extends TempModifierEvent
    {
        public final LivingEntity entity;
        public final Temperature.Trait trait;
        int count;
        Predicate<TempModifier> condition;

        public Remove(LivingEntity entity, Temperature.Trait trait, int count, Predicate<TempModifier> condition)
        {
            this.entity = entity;
            this.trait = trait;
            this.count = count;
            this.condition = condition;
        }
        public void setCount(int count) {
            this.count = count;
        }

        public void setCondition(Predicate<TempModifier> condition) {
            this.condition = condition;
        }

        public int getCount() {
            return count;
        }

        public final LivingEntity getEntity() {
            return entity;
        }

        public Predicate<TempModifier> getCondition() {
            return condition;
        }
    }


    /**
     * Fired when a TempModifier runs the {@code calculate()} method. <br>
     * {@code Pre} and {@code Post} are fired on the {@link MinecraftForge#EVENT_BUS} before/after the calculation respectively. <br>
     */
    public static class Calculate extends TempModifierEvent
    {
        public LivingEntity entity;
        public TempModifier modifier;
        public double temperature;

        public Calculate(TempModifier modifier, LivingEntity entity, double temperature)
        {
            this.entity = entity;
            this.modifier = modifier;
            this.temperature = temperature;
        }

        public TempModifier getModifier() {
            return modifier;
        }
        public double getTemperature() {
            return temperature;
        }

        /**
         * Fired at the beginning of {@code calculate()}, before the {@code getValue()} method is called. <br>
         * <br>
         * {@link #entity} - The player the TempModifier is attached to. <br>
         * {@link #modifier} - The TempModifier running the method. <br>
         * {@link #temperature} - The Temperature being passed into the {@code getValue()} method. <br>
         * <br>
         * This event is {@link Cancelable}. <br>
         * Cancelling this event results in the modifier not being processed, remaining unchanged. <br>
         */
        @Cancelable
        public static class Pre extends Calculate
        {
            public Pre(TempModifier modifier, LivingEntity entity, double temperature)
            {   super(modifier, entity, temperature);
            }
        }

        /**
         * Fired by {@code calculate()} after the {@code getResult()} method is run, but before the value is returned <br>
         * <br>
         * {@link #entity} is the player the TempModifier is attached to. <br>
         * {@link #modifier} is the TempModifier running the method. <br>
         * {@link #temperature} is the Temperature after the {@code getValue())} method has been called. <br>
         * <br>
         * This event is NOT {@link Cancelable}. <br>
         */
        public static class Post extends Calculate
        {
            public Post(TempModifier modifier, LivingEntity entity, double temperature)
            {   super(modifier, entity, temperature);
            }
        }
    }
}
