package io.github.lightman314.lightmanscurrency.api.world.data;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.text.TextEntryBundle;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;

public enum DirectionalSettingsState {
    NONE,INPUT,INPUT_AND_OUTPUT,OUTPUT;

    public static final Codec<DirectionalSettingsState> CODEC = EnumHelper.buildCodec(DirectionalSettingsState.class,"Directional Settings State");
    public static final StreamCodec<ByteBuf,DirectionalSettingsState> STREAM_CODEC = EnumHelper.buildStreamCodec(DirectionalSettingsState.class,"Directional Settings State");

    public static final TextEntryBundle<DirectionalSettingsState> TEXT = TextEntryBundle.of(values(),"gui.lightmanscurrency.settings.side.state");

    public boolean allowsInputs() { return this == INPUT || this == INPUT_AND_OUTPUT; }
    public boolean allowsOutputs() { return this == OUTPUT || this == INPUT_AND_OUTPUT; }

    public DirectionalSettingsState getNext(IDirectionalSettingsHolder object) {
        switch (this) {
            case NONE -> {
                if(object.allowInputs())
                    return INPUT;
                if(object.allowOutputs())
                    return OUTPUT;
                return NONE;
            } case INPUT -> {
                if(object.allowOutputs())
                    return INPUT_AND_OUTPUT;
                return NONE;
            }
            case INPUT_AND_OUTPUT -> { return OUTPUT; }
            case OUTPUT -> { return NONE; }
        }
        return NONE;
    }

    public DirectionalSettingsState getPrevious(IDirectionalSettingsHolder object) {
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

    public MutableComponent getText() { return TEXT.getComponent(this); }

}