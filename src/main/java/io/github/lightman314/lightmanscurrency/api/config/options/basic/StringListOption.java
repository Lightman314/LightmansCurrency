package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import java.util.List;

public class StringListOption extends ListOption<String> {

    protected StringListOption(@Nonnull NonNullSupplier<List<String>> defaultValue) { super(defaultValue); }

    @Override
    protected ConfigParser<String> getPartialParser() { return StringOption.PARSER; }

    public static StringListOption create(@Nonnull List<String> defaultValue) { return new StringListOption(() -> defaultValue); }
    public static StringListOption create(@Nonnull NonNullSupplier<List<String>> list) { return new StringListOption(list); }

}
