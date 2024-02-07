package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class BooleanOption extends ConfigOption<Boolean> {

    public static final ConfigParser<Boolean> PARSER = new Parser();

    protected BooleanOption(@Nonnull NonNullSupplier<Boolean> defaultValue) { super(defaultValue); }

    @Nonnull
    @Override
    protected ConfigParser<Boolean> getParser() { return PARSER; }

    public static BooleanOption createTrue() { return create(() -> true); }
    public static BooleanOption createFalse() { return create(() -> false); }
    public static BooleanOption create(@Nonnull NonNullSupplier<Boolean> defaultValue) { return new BooleanOption(defaultValue); }

    private static class Parser implements ConfigParser<Boolean>
    {
        @Nonnull
        @Override
        public Boolean tryParse(@Nonnull String cleanLine) throws ConfigParsingException { return Boolean.parseBoolean(cleanLine); }
        @Nonnull
        @Override
        public String write(@Nonnull Boolean value) { return Boolean.toString(value); }
    }

}
