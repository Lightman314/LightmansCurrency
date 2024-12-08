package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.util.MathUtil;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class IntOption extends ConfigOption<Integer> {

    public static ConfigParser<Integer> makeParser(int lowerLimit, int upperLimit) { return new Parser(lowerLimit, upperLimit); }

    private final int lowerLimit;
    private final int upperLimit;
    private final ConfigParser<Integer> parser;

    protected IntOption(@Nonnull Supplier<Integer> defaultValue, int lowerLimit, int upperLimit) {
        super(defaultValue);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.parser = makeParser(lowerLimit,upperLimit);
    }
    @Nonnull
    @Override
    protected ConfigParser<Integer> getParser() { return this.parser; }

    @Nonnull
    @Override
    protected List<String> bonusComments() {
        return Lists.newArrayList(
                "Range: " + this.lowerLimit + " -> " + this.upperLimit,
                "Default: " + this.getDefaultValue()
        );
    }

    public static IntOption create(int defaultValue) { return new IntOption(() -> defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public static IntOption create(int defaultValue, int lowerLimit) { return new IntOption(() -> defaultValue, lowerLimit, Integer.MAX_VALUE); }
    public static IntOption create(int defaultValue, int lowerLimit, int upperLimit) { return new IntOption(() -> defaultValue, lowerLimit, upperLimit); }
    public static IntOption create(@Nonnull Supplier<Integer> defaultValue) { return new IntOption(defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE); }
    public static IntOption create(@Nonnull Supplier<Integer> defaultValue, int lowerLimit) { return new IntOption(defaultValue, lowerLimit, Integer.MAX_VALUE); }
    public static IntOption create(@Nonnull Supplier<Integer> defaultValue, int lowerLimit, int upperLimit) { return new IntOption(defaultValue, lowerLimit, upperLimit); }



    private static class Parser implements ConfigParser<Integer>
    {
        private final int lowerLimit;
        private final int upperLimit;

        private Parser(int lowerLimit, int upperLimit) {
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
        }

        @Nonnull
        @Override
        public Integer tryParse(@Nonnull String cleanLine) throws ConfigParsingException {
            try { return MathUtil.clamp(Integer.parseInt(cleanLine), this.lowerLimit, this.upperLimit);
            } catch (NumberFormatException e) { throw new ConfigParsingException("Error parsing integer!", e); }
        }
        @Nonnull
        @Override
        public String write(@Nonnull Integer value) { return value.toString(); }
    }


}
