package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import net.minecraft.IdentifierException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class IdentifierOption extends ConfigOption<Identifier> {

    public static final ConfigParser<Identifier> PARSER = new Parser();

    protected IdentifierOption(Supplier<Identifier> defaultValue) { super(defaultValue); }

    public static IdentifierOption create(Identifier defaultValue) { return new IdentifierOption(() -> defaultValue); }
    public static IdentifierOption create(Supplier<Identifier> defaultValue) { return new IdentifierOption(defaultValue); }

    @Override
    protected ConfigParser<Identifier> getParser() { return PARSER; }

    @Nullable
    @Override
    protected String bonusComment() { return "Default: " + PARSER.write(this.getDefaultValue()); }
    @Nullable
    @Override
    protected Component bonusCommentTooltip() { return LCText.Config.CONFIG_OPTION_DEFAULT.get(PARSER.write(this.getDefaultValue())); }

    private static class Parser implements ConfigParser<Identifier>
    {
        
        @Override
        public Identifier tryParse(String cleanLine) throws ConfigParsingException {
            String s = StringOption.PARSER.tryParse(cleanLine);
            try { return Identifier.parse(s);
            } catch (IdentifierException e) { throw new ConfigParsingException(s + " is not a valid Identifier!", e); }
        }
        
        @Override
        public String write(Identifier value) { return StringOption.PARSER.write(value.toString()); }
    }
}