package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.RegistryParser;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.SimpleListSettings;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.ItemListOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.Optional;
import java.util.function.Consumer;

public class ItemListSettings extends SimpleListSettings<Item,ItemListOption> {

    public ItemListSettings(ItemListOption option, Consumer<Object> changeHandler) {
        super(option, changeHandler);
    }

    @Override
    protected Item getBackupValue() { return Items.AIR; }

    @Override
    protected Item getNewEntryValue() {
        for(Item item : BuiltInRegistries.ITEM) {
            if(this.option.allowedListValue(item))
                return item;
        }
        return Items.AIR;
    }

    @Override
    protected Optional<Item> tryCastValue(Object newValue) {
        if(newValue instanceof ItemLike item) {
            Item i = item.asItem();
            if(!this.option.allowedListValue(i))
                return Optional.empty();
            return Optional.of(i);
        }
        return Optional.empty();
    }

    @Override
    public ScrollingWidgetBuilder buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(handler -> TextBoxWrapper.builder(RegistryParser.builder(BuiltInRegistries.ITEM).requiresNamespace().withEmptyValue(Items.AIR).build(),RegistryParser::writer)
                        .withHandler(item -> {
                            if(option.allowedListValue(item))
                                handler.accept(item);
                        }))
                .optionChangeHandler(text -> {
                    Item newValue = this.getValue(index);
                    if(newValue == Items.AIR && text.getString().isEmpty())
                        return;
                    text.setStringValue(BuiltInRegistries.ITEM.getKey(newValue).toString());
                })
                .build();
    }
}
