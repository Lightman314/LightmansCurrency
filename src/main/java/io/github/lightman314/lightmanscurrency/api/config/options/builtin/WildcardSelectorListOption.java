package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.util.WildcardTargetSelector;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WildcardSelectorListOption extends ListOption<WildcardTargetSelector> {

    public WildcardSelectorListOption(Supplier<List<WildcardTargetSelector>> defaultValue) { super(defaultValue); }

    public boolean matches(ResourceLocation id) { return this.matches(id.toString()); }
    public boolean matches(String idString) { return this.get().stream().anyMatch(s -> s.matches(idString)); }

    @Override
    protected ConfigParser<WildcardTargetSelector> getPartialParser() {
        return StringOption.PARSER.map(WildcardTargetSelector::parse,WildcardTargetSelector::toString);
    }

    public static WildcardSelectorListOption create(List<WildcardTargetSelector> defaultValue) { return new WildcardSelectorListOption(() -> defaultValue); }
    public static WildcardSelectorListOption create(Supplier<List<WildcardTargetSelector>> list) { return new WildcardSelectorListOption(list); }

}