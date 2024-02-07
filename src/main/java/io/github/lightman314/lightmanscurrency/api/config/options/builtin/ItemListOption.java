package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemListOption extends ListOption<Item> {

    private final boolean allowAir;

    protected ItemListOption(@Nonnull NonNullSupplier<List<Item>> defaultValue, boolean allowAir) { super(defaultValue); this.allowAir = allowAir; }
    @Override
    protected ConfigParser<Item> getPartialParser() { return this.allowAir ? ItemOption.PARSER : ItemOption.PARSER_NO_AIR; }

    public static ItemListOption create(@Nonnull NonNullSupplier<List<Item>> defaultValue) { return new ItemListOption(defaultValue, true); }
    public static ItemListOption create(@Nonnull NonNullSupplier<List<Item>> defaultValue, boolean allowAir) { return new ItemListOption(defaultValue, allowAir); }

}
