package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class ItemListOption extends ListOption<Item> {

    private final boolean allowAir;

    protected ItemListOption(@Nonnull Supplier<List<Item>> defaultValue, boolean allowAir) { super(defaultValue); this.allowAir = allowAir; }
    @Override
    protected ConfigParser<Item> getPartialParser() { return this.allowAir ? ItemOption.PARSER : ItemOption.PARSER_NO_AIR; }

    public static ItemListOption create(@Nonnull Supplier<List<Item>> defaultValue) { return new ItemListOption(defaultValue, true); }
    public static ItemListOption create(@Nonnull Supplier<List<Item>> defaultValue, boolean allowAir) { return new ItemListOption(defaultValue, allowAir); }

}
