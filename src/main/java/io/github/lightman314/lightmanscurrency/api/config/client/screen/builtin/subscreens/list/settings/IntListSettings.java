package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.IntParser;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.SimpleListSettings;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.IntListOption;

import java.util.Optional;
import java.util.function.Consumer;

public class IntListSettings extends SimpleListSettings<Integer,IntListOption> {

    public IntListSettings(IntListOption option, Consumer<Object> changeHandler) {
        super(option, changeHandler);
    }

    @Override
    protected Integer getBackupValue() { return 0; }
    @Override
    protected Integer getNewEntryValue() { return Math.clamp(0,this.option.lowerLimit,this.option.upperLimit); }

    @Override
    protected Optional<Integer> tryCastValue(Object newValue) {
        if(newValue instanceof Number n)
            return Optional.of(n.intValue());
        return Optional.empty();
    }

    @Override
    public ScrollingWidgetBuilder buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(() -> TextBoxWrapper.intBuilder()
                        .parser(IntParser.builder().min(this.option.lowerLimit).max(this.option.upperLimit).build())
                        .withStartingValue(this.getValue(index)))
                .numberChangeHandler(() -> this.getValue(index))
                .build();
    }

}
