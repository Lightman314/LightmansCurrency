package io.github.lightman314.lightmanscurrency.api.config.options.basic;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BooleanOption extends ConfigOption<Boolean> {

    public static final ConfigParser<Boolean> PARSER = new Parser();

    protected BooleanOption(Supplier<Boolean> defaultValue) { super(defaultValue); }

    @Override
    protected ConfigParser<Boolean> getParser() { return PARSER; }

    public static BooleanOption createTrue() { return create(() -> true); }
    public static BooleanOption createFalse() { return create(() -> false); }
    public static BooleanOption create(Supplier<Boolean> defaultValue) { return new BooleanOption(defaultValue); }

    @Nullable
    @Override
    protected String bonusComment() { return "Default: " + this.getDefaultValue(); }
    @Nullable
    @Override
    protected Component bonusCommentTooltip() { return LCText.CONFIG_OPTION_DEFAULT.get(LCText.GUI_SETTINGS_VALUE_TRUE_FALSE.get(this.getDefaultValue())); }

    private static class Parser implements ConfigParser<Boolean>
    {
        
        @Override
        public Boolean tryParse(String cleanLine) { return Boolean.parseBoolean(cleanLine); }
        
        @Override
        public String write(Boolean value) { return Boolean.toString(value); }
    }

}
