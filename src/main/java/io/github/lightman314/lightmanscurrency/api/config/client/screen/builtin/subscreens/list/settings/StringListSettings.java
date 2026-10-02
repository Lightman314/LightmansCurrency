package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.SimpleListSettings;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringListOption;

import java.util.Optional;
import java.util.function.Consumer;

public class StringListSettings extends SimpleListSettings<String,StringListOption> {

    public StringListSettings(StringListOption option, Consumer<Object> changeHandler) {
        super(option, changeHandler);
    }

    @Override
    protected String getBackupValue() { return ""; }

    @Override
    protected String getNewEntryValue() { return ""; }

    @Override
    protected Optional<String> tryCastValue(Object newValue) {
        if(newValue instanceof String s)
            return Optional.of(s);
        return Optional.empty();
    }

    @Override
    public ScrollingWidgetBuilder buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(() -> TextBoxWrapper.stringBuilder()
                        .withStartingValue(this.getValue(index)))
                .optionChangeHandler(text -> text.setStringValue(this.getValue(index)))
                .build();
    }

}
