package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.StringListOption;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StringListSettings extends EasyListSettings<String, StringListOption>
{

    public StringListSettings(StringListOption option, Consumer<Object> changeHandler) { super(option,changeHandler); }

    @Override
    protected String getBackupValue() { return ""; }
    @Override
    protected String getNewEntryValue() { return ""; }
    @Override
    protected Either<String,Void> tryCastValue(Object newValue) {
        if(newValue instanceof String l)
            return Either.left(l);
        return Either.right(null);
    }

    @Override
    public AbstractWidget buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(handler -> TextInputUtil.stringBuilder()
                        .startingValue(this.getValue(index))
                        .handler(handler::accept))
                .optionChangeHandler(text -> text.setValue(this.getValue(index)))
                .build();
    }
}