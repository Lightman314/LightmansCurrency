package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class StringListOption extends ListOption<String> {

    protected StringListOption(@Nonnull Supplier<List<String>> defaultValue) { super(defaultValue); }

    @Override
    protected ConfigParser<String> getPartialParser() { return StringOption.PARSER; }

    public static StringListOption create(@Nonnull List<String> defaultValue) { return new StringListOption(() -> defaultValue); }
    public static StringListOption create(@Nonnull Supplier<List<String>> list) { return new StringListOption(list); }

}
