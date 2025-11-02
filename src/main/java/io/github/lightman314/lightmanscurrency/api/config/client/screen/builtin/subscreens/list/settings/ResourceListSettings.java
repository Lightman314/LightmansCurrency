package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.ResourceListOption;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
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
    protected ResourceLocation getNewEntryValue() { return VersionUtil.vanillaResource("null"); }

    @Override
    protected Either<ResourceLocation, Void> tryCastValue(Object newValue) {
        if(newValue instanceof ResourceLocation resource)
            return Either.left(resource);
        if(newValue instanceof String string)
        {
            try {
                return Either.left(VersionUtil.parseResource(string));
            } catch (ResourceLocationException ignored) { }
        }
        return Either.right(null);
    }

    private String getValueString(int index)
    {
        ResourceLocation value = this.getValue(index);
        return value == null ? "" : value.toString();
    }

    private Consumer<String> tryParseString(Consumer<Object> handler)
    {
        return string -> {
            try { handler.accept(VersionUtil.parseResource(string));
            } catch (ResourceLocationException ignored) {}
        };
    }

    @Override
    public AbstractWidget buildEntry(int index) {
        return ListEditBoxOption.builder(this.option,index,this)
                .inputBoxSetup(handler ->
                        TextInputUtil.stringBuilder()
                                .startingString(this.getValueString(index))
                                .handler(this.tryParseString(handler)))
                .optionChangeHandler(editBox -> editBox.setValue(this.getValueString(index)))
                .build();
    }
}