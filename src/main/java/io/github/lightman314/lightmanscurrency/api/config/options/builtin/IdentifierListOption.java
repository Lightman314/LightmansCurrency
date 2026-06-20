package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Supplier;

public class IdentifierListOption extends ListOption<Identifier> {

    protected IdentifierListOption(Supplier<List<Identifier>> defaultValue) { super(defaultValue); }

    @Override
    public boolean isValidEntryType(Class<?> clazz) { return clazz == Identifier.class; }

    @Override
    protected ConfigParser<Identifier> getPartialParser() { return IdentifierOption.PARSER; }

    public static IdentifierListOption create(List<Identifier> defaultValue) { return new IdentifierListOption(() -> defaultValue); }
    public static IdentifierListOption create(Supplier<List<Identifier>> defaultValue) { return new IdentifierListOption(defaultValue); }
}
