package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.WildcardSelectorListOption;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.util.WildcardTargetSelector;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WildcardSelectorListSettings extends EasyListSettings<WildcardTargetSelector,WildcardSelectorListOption> {

    public WildcardSelectorListSettings(WildcardSelectorListOption option, Consumer<Object> handler) { super(option,handler);  }

    @Override
    protected WildcardTargetSelector getBackupValue() { return new WildcardTargetSelector("", WildcardTargetSelector.TestType.EQUALS); }
    @Override
    protected WildcardTargetSelector getNewEntryValue() { return this.getBackupValue(); }

    @Override
    protected Either<WildcardTargetSelector, Void> tryCastValue(Object newValue) {
        if(newValue instanceof WildcardTargetSelector value)
            return Either.left(value);
        if(newValue instanceof String string)
            return Either.left(WildcardTargetSelector.parse(string));
        return Either.right(null);
    }

    private Consumer<String> tryParse(Consumer<Object> handler)
    {
        return string -> handler.accept(WildcardTargetSelector.parse(string));
    }

    @Override
    public AbstractWidget buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(handler -> TextInputUtil.stringBuilder()
                        .startingString(this.getValue(index).toString())
                        .handler(this.tryParse(handler)))
                .optionChangeHandler(editBox -> editBox.setValue(this.getValue(index).toString()))
                .build();
    }

}
