package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class DoubleListOption extends ListOption<Double> {

    private final double lowerLimit;
    private final double upperLimit;
    private final ConfigParser<Double> parser;
    protected DoubleListOption(@Nonnull NonNullSupplier<List<Double>> defaultValue, double lowerLimit, double upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = DoubleOption.makeParser(lowerLimit,upperLimit);
    }

    @Override
    protected ConfigParser<Double> getPartialParser() { return this.parser; }
    @Nullable
    @Override
    protected String bonusComment() { return "Range: " + this.lowerLimit + " -> " + this.upperLimit; }

    public static DoubleListOption create(@Nonnull List<Double> defaultValue) { return new DoubleListOption(() -> defaultValue, Double.MIN_VALUE, Double.MAX_VALUE); }
    public static DoubleListOption create(@Nonnull List<Double> defaultValue, double lowerLimit) { return new DoubleListOption(() -> defaultValue, lowerLimit, Double.MAX_VALUE); }
    public static DoubleListOption create(@Nonnull List<Double> defaultValue, double lowerLimit, double upperLimit) { return new DoubleListOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static DoubleListOption create(@Nonnull NonNullSupplier<List<Double>> defaultValue) { return new DoubleListOption(defaultValue, Double.MIN_VALUE, Double.MAX_VALUE); }
    public static DoubleListOption create(@Nonnull NonNullSupplier<List<Double>> defaultValue, double lowerLimit) { return new DoubleListOption(defaultValue, lowerLimit, Double.MAX_VALUE); }
    public static DoubleListOption create(@Nonnull NonNullSupplier<List<Double>> defaultValue, double lowerLimit, double upperLimit) { return new DoubleListOption(defaultValue, lowerLimit, upperLimit); }

}
