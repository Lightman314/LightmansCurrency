package io.github.lightman314.lightmanscurrency.api.config.options.builtin;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemListOption extends ListOption<Item> {

    public final Predicate<Item> filter;

    private final ConfigParser<Item> parser;
    protected ItemListOption(Supplier<List<Item>> defaultValue, Predicate<Item> filter) { super(defaultValue); this.filter = filter; this.parser = ItemOption.createParser(this.filter); }
    @Override
    protected ConfigParser<Item> getPartialParser() { return this.parser; }

    @Override
    public boolean allowedListValue(Item newValue) { return this.filter.test(newValue); }

    public static ItemListOption create(Supplier<List<Item>> defaultValue) { return new ItemListOption(defaultValue, i -> true); }
    public static ItemListOption create(Supplier<List<Item>> defaultValue, boolean allowAir) { return new ItemListOption(defaultValue, i -> i != Items.AIR); }
    public static ItemListOption create(Supplier<List<Item>> defaultValue, Predicate<Item> filter) { return new ItemListOption(defaultValue, filter); }

}