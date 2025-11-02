package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnumOption<T extends Enum<T>> extends ConfigOption<T> {

    public static <T extends Enum<T>> ConfigParser<T> buildParser(Class<T> clazz) { return new EnumParser<>(clazz); }

    private final ConfigParser<T> parser;
    public final Class<T> clazz;
    protected EnumOption(Supplier<T> defaultValue, Class<T> clazz) { super(defaultValue); this.parser = buildParser(clazz); this.clazz = clazz; }

    @Override
    protected ConfigParser<T> getParser() { return this.parser; }

    @Override
    protected List<String> bonusComments() {
        return Lists.newArrayList("Options: " + this.options(),"Default: " + this.getDefaultValue());
    }
    @Override
    protected List<Component> bonusCommentTooltips() {
        return Lists.newArrayList(
                LCText.CONFIG_OPTION_OPTIONS.get(this.options()),
                LCText.CONFIG_OPTION_DEFAULT.get(this.getDefaultValue().toString()));
    }

    private String options() {
        StringBuilder builder = new StringBuilder();
        for(T option : this.clazz.getEnumConstants())
        {
            if(!builder.isEmpty())
                builder.append(", ");
            builder.append(option.toString());
        }
        return builder.toString();
    }

    public static <T extends Enum<T>> EnumOption<T> create(T defaultValue) { return new EnumOption<>(() -> defaultValue, (Class<T>)defaultValue.getClass()); }
    public static <T extends Enum<T>> EnumOption<T> create(Supplier<T> defaultValue, Class<?> clazz) { return new EnumOption<>(defaultValue, (Class<T>)clazz); }

    private static class EnumParser<T extends Enum<T>> implements ConfigParser<T>
    {
        private final Class<T> clazz;
        private EnumParser(Class<T> clazz) { this.clazz = clazz; }


        @Override
        public T tryParse(String cleanLine) throws ConfigParsingException {
            T result = EnumUtil.enumFromString(cleanLine, this.clazz.getEnumConstants(), null);
            if(result == null)
                throw new ConfigParsingException(cleanLine + " is not a valid enum option!");
            return result;
        }

        @Override
        public String write(T value) { return value.name(); }
    }

}