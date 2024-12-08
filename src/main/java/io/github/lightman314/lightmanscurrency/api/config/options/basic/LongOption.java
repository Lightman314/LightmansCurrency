package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.util.MathUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class LongOption extends ConfigOption<Long> {

    public static ConfigParser<Long> makeParser(long lowerLimit, long upperLimit) { return new Parser(lowerLimit, upperLimit); }

    private final long lowerLimit;
    private final long upperLimit;
    private final ConfigParser<Long> parser;

    protected LongOption(@Nonnull Supplier<Long> defaultValue, long lowerLimit, long upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = makeParser(lowerLimit,upperLimit);
    }
    @Nonnull
    @Override
    protected ConfigParser<Long> getParser() { return this.parser; }
    @Nonnull
    @Override
    protected List<String> bonusComments() {
        return Lists.newArrayList(
                "Range: " + this.lowerLimit + " -> " + this.upperLimit,
                "Default: " + this.getDefaultValue()
        );
    }

    public static LongOption create(long defaultValue) { return new LongOption(() -> defaultValue, Long.MIN_VALUE, Long.MAX_VALUE); }
    public static LongOption create(long defaultValue, long lowerLimit) { return new LongOption(() -> defaultValue, lowerLimit, Long.MAX_VALUE); }
    public static LongOption create(long defaultValue, long lowerLimit, long upperLimit) { return new LongOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static LongOption create(@Nonnull Supplier<Long> defaultValue) { return new LongOption(defaultValue, Long.MIN_VALUE, Long.MAX_VALUE); }
    public static LongOption create(@Nonnull Supplier<Long> defaultValue, long lowerLimit) { return new LongOption(defaultValue, lowerLimit, Long.MAX_VALUE); }
    public static LongOption create(@Nonnull Supplier<Long> defaultValue, long lowerLimit, long upperLimit) { return new LongOption(defaultValue, lowerLimit, upperLimit); }



    private static class Parser implements ConfigParser<Long>
    {
        private final long lowerLimit;
        private final long upperLimit;

        private Parser(long lowerLimit, long upperLimit) {
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
        }

        @Nonnull
        @Override
        public Long tryParse(@Nonnull String cleanLine) throws ConfigParsingException {
            try { return MathUtil.clamp(Long.parseLong(cleanLine), this.lowerLimit, this.upperLimit);
            } catch (NumberFormatException e) { throw new ConfigParsingException("Error parsing long!", e); }
        }
        @Nonnull
        @Override
        public String write(@Nonnull Long value) { return Long.toString(value); }
    }


}
