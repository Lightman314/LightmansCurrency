package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class LongListOption extends ListOption<Long> {

    private final long lowerLimit;
    private final long upperLimit;
    private final ConfigParser<Long> parser;
    protected LongListOption(@Nonnull NonNullSupplier<List<Long>> defaultValue, long lowerLimit, long upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = LongOption.makeParser(lowerLimit,upperLimit);
    }

    @Override
    protected ConfigParser<Long> getPartialParser() { return this.parser; }
    @Nullable
    @Override
    protected String bonusComment() { return "Range: " + this.lowerLimit + " -> " + this.upperLimit; }

    public static LongListOption create(@Nonnull List<Long> defaultValue) { return new LongListOption(() -> defaultValue, Long.MIN_VALUE, Long.MAX_VALUE); }
    public static LongListOption create(@Nonnull List<Long> defaultValue, long lowerLimit) { return new LongListOption(() -> defaultValue, lowerLimit, Long.MAX_VALUE); }
    public static LongListOption create(@Nonnull List<Long> defaultValue, long lowerLimit, long upperLimit) { return new LongListOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static LongListOption create(@Nonnull NonNullSupplier<List<Long>> defaultValue) { return new LongListOption(defaultValue, Long.MIN_VALUE, Long.MAX_VALUE); }
    public static LongListOption create(@Nonnull NonNullSupplier<List<Long>> defaultValue, long lowerLimit) { return new LongListOption(defaultValue, lowerLimit, Long.MAX_VALUE); }
    public static LongListOption create(@Nonnull NonNullSupplier<List<Long>> defaultValue, long lowerLimit, long upperLimit) { return new LongListOption(defaultValue, lowerLimit, upperLimit); }

}
