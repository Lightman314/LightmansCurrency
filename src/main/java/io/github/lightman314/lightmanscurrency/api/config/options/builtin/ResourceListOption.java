package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class ResourceListOption extends ListOption<ResourceLocation> {

    protected ResourceListOption(@Nonnull Supplier<List<ResourceLocation>> defaultValue) { super(defaultValue); }
    @Override
    protected ConfigParser<ResourceLocation> getPartialParser() { return ResourceOption.PARSER; }

    public static ResourceListOption create(@Nonnull List<ResourceLocation> defaultValue) { return new ResourceListOption(() -> defaultValue); }
    public static ResourceListOption create(@Nonnull Supplier<List<ResourceLocation>> defaultValue) { return new ResourceListOption(defaultValue); }
}
