package io.github.lightman314.lightmanscurrency.api.misc.settings.directional;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public enum DirectionalSettingsState {
    NONE, INPUT, INPUT_AND_OUTPUT, OUTPUT;

    public boolean allowsInputs() { return this == INPUT || this == INPUT_AND_OUTPUT; }
    public boolean allowsOutputs() { return this == OUTPUT || this == INPUT_AND_OUTPUT; }

    public DirectionalSettingsState getNext(IDirectionalSettingsObject object) {
        switch (this) {
            case NONE -> {
                if(object.allowInputs())
                    return INPUT;
                if(object.allowOutputs())
                    return OUTPUT;
                return NONE;
            }
            case INPUT -> {
                if(object.allowOutputs())
                    return INPUT_AND_OUTPUT;
                return NONE;
            }
            case INPUT_AND_OUTPUT -> { return OUTPUT; }
            case OUTPUT -> { return NONE; }
        }
        return NONE;
    }

    public DirectionalSettingsState getPrevious(IDirectionalSettingsObject object) {
        switch (this) {
            case OUTPUT -> {
                if(object.allowInputs())
                    return INPUT_AND_OUTPUT;
                return NONE;
            }
            case INPUT_AND_OUTPUT -> { return INPUT; }
            case INPUT -> { return NONE; }
            case NONE -> {
                if(object.allowOutputs())
                    return OUTPUT;
                if(object.allowInputs())
                    return INPUT;
                return NONE;
            }
        }
        return NONE;
    }

    public MutableComponent getText() { return LCText.GUI_DIRECTIONAL_STATE.get(this).get(); }

    public static DirectionalSettingsState parse(String value) { return EnumUtil.enumFromString(value,values(),NONE); }

}
