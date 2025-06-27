package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.util.WildcardTargetSelector;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class WildcardSelectorListOption extends ListOption<WildcardTargetSelector> {

    public WildcardSelectorListOption(@Nonnull Supplier<List<WildcardTargetSelector>> defaultValue) { super(defaultValue); }

    public boolean matches(ResourceLocation id) { return this.matches(id.toString()); }
    public boolean matches(String idString) { return this.get().stream().anyMatch(s -> s.matches(idString)); }

    @Override
    protected ConfigParser<WildcardTargetSelector> getPartialParser() {
        return StringOption.PARSER.map(WildcardTargetSelector::parse,WildcardTargetSelector::toString);
    }

    public static WildcardSelectorListOption create(@Nonnull List<WildcardTargetSelector> defaultValue) { return new WildcardSelectorListOption(() -> defaultValue); }
    public static WildcardSelectorListOption create(@Nonnull Supplier<List<WildcardTargetSelector>> list) { return new WildcardSelectorListOption(list); }

}
