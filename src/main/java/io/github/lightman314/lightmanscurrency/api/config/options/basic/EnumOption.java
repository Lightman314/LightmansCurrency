package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class EnumOption<T extends Enum<T>> extends ConfigOption<T> {

    public static <T extends Enum<T>> ConfigParser<T> buildParser(@Nonnull Class<T> clazz) { return new EnumParser<>(clazz); }

    private final ConfigParser<T> parser;
    private final Class<T> clazz;
    protected EnumOption(@Nonnull Supplier<T> defaultValue, Class<T> clazz) { super(defaultValue); this.parser = buildParser(clazz); this.clazz = clazz; }

    @Nonnull
    @Override
    protected ConfigParser<T> getParser() { return this.parser; }

    @Nonnull
    @Override
    protected List<String> bonusComments() {
        return Lists.newArrayList(this.bonusComment(),"Default: " + this.getDefaultValue());
    }

    @Nullable
    @Override
    protected String bonusComment() {
        StringBuilder builder = new StringBuilder("Options: ");
        boolean comma = false;
        for(T option : clazz.getEnumConstants())
        {
            if(comma)
                builder.append(", ");
            else
                comma = true;
            builder.append(option.name());
        }
        return builder.toString();
    }

    public static <T extends Enum<T>> EnumOption<T> create(@Nonnull T defaultValue) { return new EnumOption<>(() -> defaultValue, (Class<T>)defaultValue.getClass()); }
    public static <T extends Enum<T>> EnumOption<T> create(@Nonnull Supplier<T> defaultValue, @Nonnull Class<?> clazz) { return new EnumOption<>(defaultValue, (Class<T>)clazz); }

    private static class EnumParser<T extends Enum<T>> implements ConfigParser<T>
    {
        private final Class<T> clazz;
        private EnumParser(@Nonnull Class<T> clazz) { this.clazz = clazz; }

        @Nonnull
        @Override
        public T tryParse(@Nonnull String cleanLine) throws ConfigParsingException {
            T result = EnumUtil.enumFromString(cleanLine, this.clazz.getEnumConstants(), null);
            if(result == null)
                throw new ConfigParsingException(cleanLine + " is not a valid enum option!");
            return result;
        }
        @Nonnull
        @Override
        public String write(@Nonnull T value) { return value.name(); }
    }

}
