package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ResourceOption extends ConfigOption<ResourceLocation> {

    public static final ConfigParser<ResourceLocation> PARSER = new Parser();

    protected ResourceOption(Supplier<ResourceLocation> defaultValue) { super(defaultValue); }

    public static ResourceOption create(ResourceLocation defaultValue) { return new ResourceOption(() -> defaultValue); }
    public static ResourceOption create(Supplier<ResourceLocation> defaultValue) { return new ResourceOption(defaultValue); }

    @Override
    protected ConfigParser<ResourceLocation> getParser() { return PARSER; }

    @Nullable
    @Override
    protected String bonusComment() { return "Default: " + PARSER.write(this.getDefaultValue()); }
    @Nullable
    @Override
    protected Component bonusCommentTooltip() { return LCText.CONFIG_OPTION_DEFAULT.get(PARSER.write(this.getDefaultValue())); }

    private static class Parser implements ConfigParser<ResourceLocation>
    {

        
        @Override
        public ResourceLocation tryParse(String cleanLine) throws ConfigParsingException {
            String s = StringOption.PARSER.tryParse(cleanLine);
            try { return VersionUtil.parseResource(s);
            } catch (ResourceLocationException e) { throw new ConfigParsingException(s + " is not a valid Resource Location!", e); }
        }

        
        @Override
        public String write(ResourceLocation value) { return StringOption.PARSER.write(value.toString()); }
    }
}
