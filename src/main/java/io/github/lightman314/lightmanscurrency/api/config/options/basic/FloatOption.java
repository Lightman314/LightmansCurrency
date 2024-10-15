package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.util.MathUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class FloatOption extends ConfigOption<Float> {

    public static ConfigParser<Float> makeParser(float lowerLimit, float upperLimit) { return new Parser(lowerLimit, upperLimit); }

    private final float lowerLimit;
    private final float upperLimit;
    private final ConfigParser<Float> parser;

    protected FloatOption(@Nonnull Supplier<Float> defaultValue, float lowerLimit, float upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = makeParser(lowerLimit,upperLimit);
    }
    @Nonnull
    @Override
    protected ConfigParser<Float> getParser() { return this.parser; }
    @Nullable
    @Override
    protected String bonusComment() { return "Range: " + this.lowerLimit + " -> " + this.upperLimit; }

    public static FloatOption create(float defaultValue) { return new FloatOption(() -> defaultValue, -Float.MAX_VALUE, Float.MAX_VALUE); }
    public static FloatOption create(float defaultValue, float lowerLimit) { return new FloatOption(() -> defaultValue, lowerLimit, Float.MAX_VALUE); }
    public static FloatOption create(float defaultValue, float lowerLimit, float upperLimit) { return new FloatOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static FloatOption create(@Nonnull Supplier<Float> defaultValue) { return new FloatOption(defaultValue, -Float.MAX_VALUE, Float.MAX_VALUE); }
    public static FloatOption create(@Nonnull Supplier<Float> defaultValue, float lowerLimit) { return new FloatOption(defaultValue, lowerLimit, Float.MAX_VALUE); }
    public static FloatOption create(@Nonnull Supplier<Float> defaultValue, float lowerLimit, float upperLimit) { return new FloatOption(defaultValue, lowerLimit, upperLimit); }



    private static class Parser implements ConfigParser<Float>
    {
        private final float lowerLimit;
        private final float upperLimit;

        private Parser(float lowerLimit, float upperLimit) {
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
        }

        @Nonnull
        @Override
        public Float tryParse(@Nonnull String cleanLine) throws ConfigParsingException {
            try { return MathUtil.clamp(Float.parseFloat(cleanLine), this.lowerLimit, this.upperLimit);
            } catch (NumberFormatException e) { throw new ConfigParsingException("Error parsing float!", e); }
        }
        @Nonnull
        @Override
        public String write(@Nonnull Float value) { return value.toString(); }
    }


}
