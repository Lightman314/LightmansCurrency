package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.subscreens.MoneyValueListConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListButtonOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.MoneyValueListOption;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MoneyValueListSettings extends EasyListSettings<MoneyValue,MoneyValueListOption> {

    public MoneyValueListSettings(MoneyValueListOption option, Consumer<Object> changeHandler) { super(option, changeHandler); }

    @Override
    protected MoneyValue getBackupValue() { return MoneyValue.empty(); }
    @Override
    protected MoneyValue getNewEntryValue() { return this.getBackupValue(); }

    @Override
    protected Either<MoneyValue, Void> tryCastValue(Object newValue) {
        if(newValue instanceof MoneyValue value)
            return Either.left(value);
        return Either.right(null);
    }

    @Override
    public AbstractWidget buildEntry(int index) {
        return ListButtonOption.builder(this.option,index,this)
                .buttonText(() -> this.getValue(index).getText(LCText.GUI_MONEY_STORAGE_EMPTY.get()))
                .openScreen(handler -> new MoneyValueListConfigScreen(this.getScreen(),this.getScreen().file,this.option,index,handler))
                .build();
    }

}