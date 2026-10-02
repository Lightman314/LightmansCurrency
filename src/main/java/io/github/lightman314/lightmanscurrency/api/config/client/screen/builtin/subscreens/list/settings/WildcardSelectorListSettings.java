package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.SimpleListSettings;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.WildcardSelectorListOption;
import io.github.lightman314.lightmanscurrency.api.helpers.data.WildcardTargetSelector;

import java.util.Optional;
import java.util.function.Consumer;

public class WildcardSelectorListSettings extends SimpleListSettings<WildcardTargetSelector,WildcardSelectorListOption> {

    public WildcardSelectorListSettings(WildcardSelectorListOption option, Consumer<Object> changeHandler) {
        super(option, changeHandler);
    }

    @Override
    protected WildcardTargetSelector getBackupValue() { return new WildcardTargetSelector("",WildcardTargetSelector.TestType.EQUALS); }

    @Override
    protected WildcardTargetSelector getNewEntryValue() { return this.getBackupValue(); }

    @Override
    protected Optional<WildcardTargetSelector> tryCastValue(Object newValue) {
        if(newValue instanceof WildcardTargetSelector s)
            return Optional.of(s);
        if(newValue instanceof String s)
            return Optional.of(WildcardTargetSelector.parse(s));
        return Optional.empty();
    }

    private Consumer<String> tryParse(Consumer<Object> handler) { return string -> handler.accept(WildcardTargetSelector.parse(string)); }

    @Override
    public ScrollingWidgetBuilder buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(handler -> TextBoxWrapper.stringBuilder()
                        .withStartingValue(this.getValue(index).toString())
                        .withHandler(this.tryParse(handler)))
                .optionChangeHandler(text -> text.setStringValue(this.getValue(index).toString()))
                .build();
    }
}
