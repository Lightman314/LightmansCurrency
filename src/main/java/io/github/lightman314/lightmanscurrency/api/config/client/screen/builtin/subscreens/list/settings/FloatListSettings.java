package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.parsers.FloatParser;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.SimpleListSettings;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.FloatListOption;

import java.util.Optional;
import java.util.function.Consumer;

public class FloatListSettings extends SimpleListSettings<Float,FloatListOption> {

    public FloatListSettings(FloatListOption option, Consumer<Object> changeHandler) {
        super(option, changeHandler);
    }

    @Override
    protected Float getBackupValue() { return 0f; }

    @Override
    protected Float getNewEntryValue() { return Math.clamp(0f,this.option.lowerLimit,this.option.upperLimit); }

    @Override
    protected Optional<Float> tryCastValue(Object newValue) {
        if(newValue instanceof Number n)
            return Optional.of(n.floatValue());
        return Optional.empty();
    }

    @Override
    public ScrollingWidgetBuilder buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(() -> TextBoxWrapper.floatBuilder()
                        .parser(FloatParser.builder().max(this.option.lowerLimit).max(this.option.upperLimit).build())
                        .withStartingValue(this.getValue(index)))
                .numberChangeHandler(() -> this.getValue(index))
                .build();
    }

}