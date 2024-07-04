package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class StringOption extends ConfigOption<String> {

    public static final ConfigParser<String> PARSER = new Parser();

    protected StringOption(@Nonnull Supplier<String> defaultValue) { super(defaultValue); }

    @Nonnull
    public static StringOption create(@Nonnull String defaultValue) { return new StringOption(() -> defaultValue); }
    public static StringOption create(@Nonnull Supplier<String> defaultValue) { return new StringOption(defaultValue); }

    @Nonnull
    @Override
    protected ConfigParser<String> getParser() { return PARSER; }

    private static class Parser implements ConfigParser<String>
    {
        @Nonnull
        @Override
        public String tryParse(@Nonnull String cleanLine) {
            if(cleanLine.startsWith("\"") && cleanLine.endsWith("\""))
                return cleanLine.substring(1,cleanLine.length() - 1);
            return cleanLine;
        }
        @Nonnull
        @Override
        public String write(@Nonnull String value) { return '"' + value + '"'; }
    }

}
