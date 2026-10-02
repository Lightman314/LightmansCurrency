package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.SimpleListSettings;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.subscreens.MoneyValueListConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListButtonOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.MoneyValueListOption;
import io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;

import java.util.Optional;
import java.util.function.Consumer;

public class MoneyValueListSettings extends SimpleListSettings<MoneyValue,MoneyValueListOption> {

    public MoneyValueListSettings(MoneyValueListOption option, Consumer<Object> changeHandler) {
        super(option, changeHandler);
    }

    @Override
    protected MoneyValue getBackupValue() { return MoneyValue.empty(); }

    @Override
    protected MoneyValue getNewEntryValue() { return this.getBackupValue(); }

    @Override
    protected Optional<MoneyValue> tryCastValue(Object newValue) {
        if(newValue instanceof MoneyValue value)
            return Optional.of(value);
        return Optional.empty();
    }

    @Override
    public ScrollingWidgetBuilder buildEntry(int index) {
        return ListButtonOption.builder(this.option,index,this)
                .buttonText(() -> this.getValue(index).getText(MoneyDisplayHelper.GUI_MONEY_STORAGE_EMPTY.get()))
                .openScreen(handler -> new MoneyValueListConfigScreen(this.getScreen(),this.getScreen().file,this.option,index,handler))
                .build();
    }
}
