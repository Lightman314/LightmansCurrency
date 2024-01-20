package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class ResourceOption extends ConfigOption<ResourceLocation> {

    public static final ConfigParser<ResourceLocation> PARSER = new Parser();

    protected ResourceOption(@Nonnull NonNullSupplier<ResourceLocation> defaultValue) { super(defaultValue); }

    public static ResourceOption create(@Nonnull ResourceLocation defaultValue) { return new ResourceOption(() -> defaultValue); }
    public static ResourceOption create(@Nonnull NonNullSupplier<ResourceLocation> defaultValue) { return new ResourceOption(defaultValue); }

    @Nonnull
    @Override
    protected ConfigParser<ResourceLocation> getParser() { return PARSER; }

    private static class Parser implements ConfigParser<ResourceLocation>
    {

        @Nonnull
        @Override
        public ResourceLocation tryParse(@Nonnull String cleanLine) throws ConfigParsingException {
            String s = StringOption.PARSER.tryParse(cleanLine);
            try { return new ResourceLocation(s);
            } catch (ResourceLocationException e) { throw new ConfigParsingException(s + " is not a valid Resource Location!"); }
        }

        @Nonnull
        @Override
        public String write(@Nonnull ResourceLocation value) { return StringOption.PARSER.write(value.toString()); }
    }
}
