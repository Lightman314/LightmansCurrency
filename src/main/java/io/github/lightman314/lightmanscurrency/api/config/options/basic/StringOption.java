package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StringOption extends ConfigOption<String> {

    public static final ConfigParser<String> PARSER = new Parser();

    protected StringOption(Supplier<String> defaultValue) { super(defaultValue); }

    public static StringOption create(String defaultValue) { return new StringOption(() -> defaultValue); }
    public static StringOption create(Supplier<String> defaultValue) { return new StringOption(defaultValue); }

    @Override
    protected ConfigParser<String> getParser() { return PARSER; }

    @Nullable
    @Override
    protected String bonusComment() { return "Default: " + PARSER.write(this.getDefaultValue()); }

    private static class Parser implements ConfigParser<String>
    {
        
        @Override
        public String tryParse(String cleanLine) throws ConfigParsingException {
            if(cleanLine.startsWith("\"") && cleanLine.endsWith("\""))
                return cleanLine.substring(1,cleanLine.length() - 1);
            return cleanLine;
        }
        
        @Override
        public String write(String value) { return '"' + value + '"'; }
    }

}
