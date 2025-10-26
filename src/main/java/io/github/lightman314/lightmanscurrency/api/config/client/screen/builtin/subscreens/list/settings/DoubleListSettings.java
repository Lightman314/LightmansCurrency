package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.DoubleListOption;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.DoubleParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DoubleListSettings extends EasyListSettings<Double, DoubleListOption>
{

    public DoubleListSettings(DoubleListOption option, Consumer<Object> changeHandler) { super(option,changeHandler); }

    @Override
    protected Double getBackupValue() { return 0d; }

    @Override
    protected Double getNewEntryValue() { return Math.clamp(0d,this.option.lowerLimit,this.option.upperLimit); }

    @Override
    protected Either<Double, Void> tryCastValue(Object newValue) {
        if(newValue instanceof Double d)
            return Either.left(d);
        return Either.right(null);
    }

    @Override
    public AbstractWidget buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(handler -> TextInputUtil.doubleBuilder()
                        .apply(DoubleParser.builder().min(this.option.lowerLimit).max(this.option.upperLimit).consumer())
                        .startingValue(this.getValue(index))
                        .handler(handler::accept))
                .optionChangeHandler(text -> {
                    double newValue = this.getValue(index);
                    if(newValue == 0 && text.getValue().isEmpty())
                        return;
                    if(newValue == 0)
                        text.setValue("0");
                    else
                        text.setValue(String.valueOf(newValue));
                })
                .build();
    }
}
