package io.github.lightman314.lightmanscurrency.network.message.config;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.MapLikeOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketEditMapConfig extends ServerToClientPacket {

    public static final Handler<SPacketEditMapConfig> HANDLER = new H();

    private final ResourceLocation fileID;
    private final String option;
    private final String input;
    private final String key;
    private final boolean isSet;

    public SPacketEditMapConfig(ResourceLocation fileID, String option, String input, String key, boolean isSet)
    {
        this.fileID = fileID;
        this.option = option;
        this.input = input;
        this.key = key;
        this.isSet = isSet;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.fileID);
        buffer.writeUtf(this.option);
        buffer.writeUtf(this.input);
        buffer.writeUtf(this.key);
        buffer.writeBoolean(this.isSet);
    }

    private static class H extends Handler<SPacketEditMapConfig>
    {
        @Override
        public SPacketEditMapConfig decode(FriendlyByteBuf buffer) {
            return new SPacketEditMapConfig(buffer.readResourceLocation(),buffer.readUtf(), buffer.readUtf(), buffer.readUtf(), buffer.readBoolean());
        }

        @Override
        protected void handle(SPacketEditMapConfig message, Player sender) {
            ConfigFile file = ConfigFile.lookupFile(message.fileID);
            if(file != null && file.isClientOnly())
            {
                Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
                if(optionMap.containsKey(message.option) && optionMap.get(message.option) instanceof MapLikeOption<?> option)
                {
                    Pair<Boolean,ConfigParsingException> result = option.editMap(message.input, message.key, message.isSet);
                    if(!result.getFirst())
                    {
                        LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_FAIL_PARSE.get(result.getSecond().getMessage()).withStyle(ChatFormatting.RED));
                        return;
                    }
                    if(!message.isSet)
                        LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_LIST_REMOVE_SUCCESS.get(message.option + "[" + message.key + "]"));
                    LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_EDIT_SUCCESS.get(message.option + "[" + message.key + "]", message.input));
                    return;
                }
                LightmansCurrency.getProxy().sendClientMessage(LCText.COMMAND_CONFIG_FAIL_MISSING.get(message.option).withStyle(ChatFormatting.RED));
            }
        }

    }

}