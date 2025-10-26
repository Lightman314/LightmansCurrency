package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StringListOption extends ListOption<String> {

    protected StringListOption(Supplier<List<String>> defaultValue) { super(defaultValue); }

    @Override
    protected ConfigParser<String> getPartialParser() { return StringOption.PARSER; }

    public static StringListOption create(List<String> defaultValue) { return new StringListOption(() -> defaultValue); }
    public static StringListOption create(Supplier<List<String>> list) { return new StringListOption(list); }

}
