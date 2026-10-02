package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.SimpleListSettings;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.IdentifierListOption;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.function.Consumer;

public class IdentifierListSettings extends SimpleListSettings<Identifier,IdentifierListOption> {

    public IdentifierListSettings(IdentifierListOption option, Consumer<Object> changeHandler) {
        super(option, changeHandler);
    }

    @Override
    protected Identifier getBackupValue() { return null; }

    @Override
    protected Identifier getNewEntryValue() { return Identifier.withDefaultNamespace("null"); }

    @Override
    protected Optional<Identifier> tryCastValue(Object newValue) {
        if(newValue instanceof Identifier id)
            return Optional.of(id);
        if(newValue instanceof String s) {
            try { return Optional.of(Identifier.parse(s));
            }catch (IdentifierException ignored) {}
        }
        return Optional.empty();
    }

    private String getValueString(int index) {
        Identifier value = this.getValue(index);
        return value == null ? "" : value.toString();
    }

    @Override
    public ScrollingWidgetBuilder buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(() -> TextBoxWrapper.identifierBuilder(true)
                        .withStartingValue(this.getValue(index)))
                .optionChangeHandler(text -> text.setStringValue(this.getValueString(index)))
                .build();
    }
}
