package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.LongListOption;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.LongParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LongListSettings extends EasyListSettings<Long, LongListOption>
{

    public LongListSettings(LongListOption option, Consumer<Object> changeHandler) { super(option,changeHandler); }

    @Override
    protected Long getBackupValue() { return 0L; }
    @Override
    protected Long getNewEntryValue() { return Math.clamp(0,this.option.lowerLimit,this.option.upperLimit); }
    @Override
    protected Either<Long,Void> tryCastValue(Object newValue) {
        if(newValue instanceof Long l)
            return Either.left(l);
        return Either.right(null);
    }

    @Override
    public AbstractWidget buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(handler -> TextInputUtil.longBuilder()
                        .apply(LongParser.builder().min(this.option.lowerLimit).max(this.option.upperLimit).consumer())
                        .startingValue(this.getValue(index))
                        .handler(handler::accept))
                .optionChangeHandler(text -> {
                    float newValue = this.getValue(index);
                    if(newValue == 0 && text.getValue().isEmpty())
                        return;
                    text.setValue(String.valueOf(newValue));
                })
                .build();
    }
}