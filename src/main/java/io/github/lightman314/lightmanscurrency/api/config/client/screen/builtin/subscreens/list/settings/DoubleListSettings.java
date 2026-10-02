package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.DoubleParser;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.SimpleListSettings;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.DoubleListOption;

import java.util.Optional;
import java.util.function.Consumer;

public class DoubleListSettings extends SimpleListSettings<Double,DoubleListOption> {

    public DoubleListSettings(DoubleListOption option, Consumer<Object> changeHandler) { super(option, changeHandler); }

    @Override
    protected Double getBackupValue() { return 0d;}
    @Override
    protected Double getNewEntryValue() { return Math.clamp(0d,this.option.lowerLimit,this.option.upperLimit); }

    @Override
    protected Optional<Double> tryCastValue(Object newValue) {
        if(newValue instanceof Number n)
            return Optional.of(n.doubleValue());
        return Optional.empty();
    }

    @Override
    public ScrollingWidgetBuilder buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(() -> TextBoxWrapper.doubleBuilder()
                        .parser(DoubleParser.builder().min(option.lowerLimit).max(option.upperLimit).build())
                        .withStartingValue(this.getValue(index)))
                .numberChangeHandler(() -> this.getValue(index))
                .build();
    }

}
