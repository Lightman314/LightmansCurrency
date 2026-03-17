package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.ResourceListOption;
import io.github.lightman314.lightmanscurrency.api.client.widgets.text_inputs.TextInputUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ResourceListSettings extends EasyListSettings<ResourceLocation,ResourceListOption> {

    public ResourceListSettings(ResourceListOption option, Consumer<Object> changeHandler) { super(option, changeHandler); }

    @Override
    protected ResourceLocation getBackupValue() { return null; }
    @Override
    protected ResourceLocation getNewEntryValue() { return ResourceLocation.withDefaultNamespace("null"); }

    @Override
    protected Either<ResourceLocation, Void> tryCastValue(Object newValue) {
        if(newValue instanceof ResourceLocation resource)
            return Either.left(resource);
        if(newValue instanceof String string)
        {
            try {
                return Either.left(ResourceLocation.parse(string));
            } catch (ResourceLocationException ignored) { }
        }
        return Either.right(null);
    }

    private String getValueString(int index)
    {
        ResourceLocation value = this.getValue(index);
        return value == null ? "" : value.toString();
    }

    @Override
    public AbstractWidget buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(handler ->
                    TextInputUtil.resourceBuilder(true)
                            .startingValue(this.getValue(index))
                            .handler(handler::accept))
                .optionChangeHandler(editBox -> editBox.setValue(this.getValueString(index)))
                .build();
    }
}
