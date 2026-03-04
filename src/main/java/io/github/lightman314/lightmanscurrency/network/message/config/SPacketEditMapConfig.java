package io.github.lightman314.lightmanscurrency.network.message.config;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.MapLikeOption;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public class SPacketEditMapConfig extends ServerToClientPacket {

    private static final Type<SPacketEditMapConfig> TYPE = sType("config_edit_map");
    private static final StreamCodec<ByteBuf,SPacketEditMapConfig> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,p -> p.fileID,
            ByteBufCodecs.STRING_UTF8,p -> p.option,
            ByteBufCodecs.STRING_UTF8,p -> p.input,
            ByteBufCodecs.STRING_UTF8,p -> p.key,
            ByteBufCodecs.BOOL,p -> p.isSet,
            SPacketEditMapConfig::new);
    public static final Handler<SPacketEditMapConfig> HANDLER = new H();

    private final ResourceLocation fileID;
    private final String option;
    private final String input;
    private final String key;
    private final boolean isSet;

    public SPacketEditMapConfig(ResourceLocation fileID, String option, String input, String key, boolean isSet)
    {
        super(TYPE);
        this.fileID = fileID;
        this.option = option;
        this.input = input;
        this.key = key;
        this.isSet = isSet;
    }

    private static class H extends Handler<SPacketEditMapConfig>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(SPacketEditMapConfig message, IPayloadContext context, Player player) {
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
