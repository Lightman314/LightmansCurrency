package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class IntListOption extends ListOption<Integer> {

    private final int lowerLimit;
    private final int upperLimit;
    private final ConfigParser<Integer> parser;
    protected IntListOption(@Nonnull Supplier<List<Integer>> defaultValue, int lowerLimit, int upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = IntOption.makeParser(lowerLimit,upperLimit);
    }

    @Override
    protected ConfigParser<Integer> getPartialParser() { return this.parser; }
    @Nullable
    @Override
    protected String bonusComment() { return "Range: " + this.lowerLimit + " -> " + this.upperLimit; }

    public static IntListOption create(@Nonnull List<Integer> defaultValue) { return new IntListOption(() -> defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public static IntListOption create(@Nonnull List<Integer> defaultValue, int lowerLimit) { return new IntListOption(() -> defaultValue, lowerLimit, Integer.MAX_VALUE); }
    public static IntListOption create(@Nonnull List<Integer> defaultValue, int lowerLimit, int upperLimit) { return new IntListOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static IntListOption create(@Nonnull Supplier<List<Integer>> defaultValue) { return new IntListOption(defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public static IntListOption create(@Nonnull Supplier<List<Integer>> defaultValue, int lowerLimit) { return new IntListOption(defaultValue, lowerLimit, Integer.MAX_VALUE); }
    public static IntListOption create(@Nonnull Supplier<List<Integer>> defaultValue, int lowerLimit, int upperLimit) { return new IntListOption(defaultValue, lowerLimit, upperLimit); }

}
