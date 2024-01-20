package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class IntListOption extends ListOption<Integer> {

    private final int lowerLimit;
    private final int upperLimit;
    private final ConfigParser<Integer> parser;
    protected IntListOption(@Nonnull NonNullSupplier<List<Integer>> defaultValue, int lowerLimit, int upperLimit) {
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
    public static IntListOption create(@Nonnull NonNullSupplier<List<Integer>> defaultValue) { return new IntListOption(defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public static IntListOption create(@Nonnull NonNullSupplier<List<Integer>> defaultValue, int lowerLimit) { return new IntListOption(defaultValue, lowerLimit, Integer.MAX_VALUE); }
    public static IntListOption create(@Nonnull NonNullSupplier<List<Integer>> defaultValue, int lowerLimit, int upperLimit) { return new IntListOption(defaultValue, lowerLimit, upperLimit); }

}
