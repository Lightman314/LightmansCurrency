package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class FloatListOption extends ListOption<Float> {

    private final float lowerLimit;
    private final float upperLimit;
    private final ConfigParser<Float> parser;
    protected FloatListOption(@Nonnull NonNullSupplier<List<Float>> defaultValue, float lowerLimit, float upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = FloatOption.makeParser(lowerLimit,upperLimit);
    }

    @Override
    protected ConfigParser<Float> getPartialParser() { return this.parser; }
    @Nullable
    @Override
    protected String bonusComment() { return "Range: " + this.lowerLimit + " -> " + this.upperLimit; }

    public static FloatListOption create(@Nonnull List<Float> defaultValue) { return new FloatListOption(() -> defaultValue, Float.MIN_VALUE, Float.MAX_VALUE); }
    public static FloatListOption create(@Nonnull List<Float> defaultValue, float lowerLimit) { return new FloatListOption(() -> defaultValue, lowerLimit, Float.MAX_VALUE); }
    public static FloatListOption create(@Nonnull List<Float> defaultValue, float lowerLimit, float upperLimit) { return new FloatListOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static FloatListOption create(@Nonnull NonNullSupplier<List<Float>> defaultValue) { return new FloatListOption(defaultValue, Float.MIN_VALUE, Float.MAX_VALUE); }
    public static FloatListOption create(@Nonnull NonNullSupplier<List<Float>> defaultValue, float lowerLimit) { return new FloatListOption(defaultValue, lowerLimit, Float.MAX_VALUE); }
    public static FloatListOption create(@Nonnull NonNullSupplier<List<Float>> defaultValue, float lowerLimit, float upperLimit) { return new FloatListOption(defaultValue, lowerLimit, upperLimit); }

}
