package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class DoubleListOption extends ListOption<Double> {

    public final double lowerLimit;
    public final double upperLimit;
    private final ConfigParser<Double> parser;
    protected DoubleListOption(Supplier<List<Double>> defaultValue, double lowerLimit, double upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = DoubleOption.makeParser(lowerLimit,upperLimit);
    }

    @Override
    public boolean allowedListValue(Double newValue) { return newValue >= this.lowerLimit && newValue <= this.upperLimit; }

    @Override
    protected ConfigParser<Double> getPartialParser() { return this.parser; }
    @Nullable
    @Override
    protected String bonusComment() { return "Range: " + this.lowerLimit + " -> " + this.upperLimit; }
    @Nullable
    @Override
    protected Component bonusCommentTooltip() { return LCText.Config.CONFIG_OPTION_RANGE.get(this.lowerLimit,this.upperLimit); }

    public static DoubleListOption create(List<Double> defaultValue) { return new DoubleListOption(() -> defaultValue, -Double.MAX_VALUE, Double.MAX_VALUE); }
    public static DoubleListOption create(List<Double> defaultValue, double lowerLimit) { return new DoubleListOption(() -> defaultValue, lowerLimit, Double.MAX_VALUE); }
    public static DoubleListOption create(List<Double> defaultValue, double lowerLimit, double upperLimit) { return new DoubleListOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static DoubleListOption create(Supplier<List<Double>> defaultValue) { return new DoubleListOption(defaultValue, -Double.MAX_VALUE, Double.MAX_VALUE); }
    public static DoubleListOption create(Supplier<List<Double>> defaultValue, double lowerLimit) { return new DoubleListOption(defaultValue, lowerLimit, Double.MAX_VALUE); }
    public static DoubleListOption create(Supplier<List<Double>> defaultValue, double lowerLimit, double upperLimit) { return new DoubleListOption(defaultValue, lowerLimit, upperLimit); }

    @Override
    public boolean isValidEntryType(Class<?> clazz) { return clazz == Double.class; }

}