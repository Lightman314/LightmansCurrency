package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.ItemListOption;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemListSettings extends EasyListSettings<Item,ItemListOption> {

    public ItemListSettings(ItemListOption option, Consumer<Object> changeHandler) { super(option, changeHandler); }

    @Override
    protected Item getBackupValue() { return Items.AIR; }
    @Override
    protected Item getNewEntryValue() {
        for(Item item : BuiltInRegistries.ITEM)
        {
            if(this.option.allowedListValue(item))
                return item;
        }
        return null;
    }

    @Override
    protected Either<Item, Void> tryCastValue(Object newValue) {
        if(newValue instanceof ItemLike item)
        {
            Item i = item.asItem();
            if(!this.option.filter.test(i))
                return Either.right(null);
            return Either.left(i);
        }
        return Either.right(null);
    }

    private Consumer<ResourceLocation> tryParseItem(Consumer<Object> consumer)
    {
        return id -> {
            if(BuiltInRegistries.ITEM.containsKey(id))
            {
                Item item = BuiltInRegistries.ITEM.get(id);
                if(this.option.allowedListValue(item))
                    consumer.accept(item);
            }
        };
    }

    @Override
    public AbstractWidget buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(handler -> TextInputUtil.resourceBuilder(true)
                        .startingValue(BuiltInRegistries.ITEM.getKey(this.getValue(index)))
                        .handler(this.tryParseItem(handler)))
                .optionChangeHandler(editBox -> editBox.setValue(BuiltInRegistries.ITEM.getKey(this.getValue(index)).toString()))
                .build();
    }

}
