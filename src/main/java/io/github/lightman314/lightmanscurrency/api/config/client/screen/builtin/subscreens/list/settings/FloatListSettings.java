package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.FloatListOption;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.FloatParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FloatListSettings extends EasyListSettings<Float, FloatListOption>
{

    public FloatListSettings(FloatListOption option, Consumer<Object> changeHandler) { super(option,changeHandler); }

    @Override
    protected Float getBackupValue() { return 0f; }

    @Override
    protected Float getNewEntryValue() { return MathUtil.clamp(0f,this.option.lowerLimit,this.option.upperLimit); }

    @Override
    protected Either<Float,Void> tryCastValue(Object newValue) {
        if(newValue instanceof Float f)
            return Either.left(f);
        return Either.right(null);
    }

    @Override
    public AbstractWidget buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(handler -> TextInputUtil.floatBuilder()
                        .apply(FloatParser.builder().min(this.option.lowerLimit).max(this.option.upperLimit).consumer())
                        .startingValue(this.getValue(index))
                        .handler(handler::accept))
                .optionChangeHandler(text -> {
                    float newValue = this.getValue(index);
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