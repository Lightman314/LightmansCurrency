package io.github.lightman314.lightmanscurrency.client.config;

import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public class ItemTestListOption extends ListOption<ItemTest> {

    private ItemTestListOption(Supplier<List<ItemTest>> defaultValue) {
        super(defaultValue);
    }

    public static ItemTestListOption create(List<ItemTest> defaultValue) { return create(() -> defaultValue); }
    public static ItemTestListOption create(Supplier<List<ItemTest>> defaultValue) { return new ItemTestListOption(defaultValue); }

    public boolean contains(ItemStack stack) { return this.getCurrentValue().stream().anyMatch(test -> test.test(stack)); }

    @Override
    protected ConfigParser<ItemTest> getPartialParser() { return StringOption.PARSER.map(ItemTest::parse,ItemTest::toString); }

}