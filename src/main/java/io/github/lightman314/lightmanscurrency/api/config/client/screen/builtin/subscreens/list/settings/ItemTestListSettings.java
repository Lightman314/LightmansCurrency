package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.client.config.ItemTest;
import io.github.lightman314.lightmanscurrency.client.config.ItemTestListOption;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class ItemTestListSettings extends EasyListSettings<ItemTest,ItemTestListOption> {

    public ItemTestListSettings(ItemTestListOption option, Consumer<Object> changeHandler) { super(option, changeHandler); }

    @Override
    protected ItemTest getBackupValue() { return this.getNewEntryValue(); }
    @Override
    protected ItemTest getNewEntryValue() { return ItemTest.create(Items.AIR); }
    @Override
    protected Either<ItemTest, Void> tryCastValue(Object newValue) {
        if(newValue instanceof ItemTest test)
            return Either.left(test);
        if(newValue instanceof String string)
        {
            ItemTest test = ItemTest.tryParseTest(string);
            if(test != null)
                return Either.left(test);
        }
        return Either.right(null);
    }

    private Consumer<String> handleInput(Consumer<Object> handler)
    {
        return string -> {
            ItemTest test = ItemTest.tryParseTest(string);
            if(test != null)
                handler.accept(test);
        };
    }

    @Override
    public AbstractWidget buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(handler -> TextInputUtil.stringBuilder()
                        .startingValue(this.getValue(index).toString())
                        .handler(this.handleInput(handler)))
                .optionChangeHandler(text -> text.setValue(this.getValue(index).toString()))
                .build();
    }

}