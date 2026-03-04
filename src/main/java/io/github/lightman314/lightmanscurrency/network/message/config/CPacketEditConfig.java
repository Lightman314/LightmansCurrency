package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public class CPacketEditConfig extends ClientToServerPacket {

    private static final Type<CPacketEditConfig> TYPE = cType("config_edit");
    private static final StreamCodec<ByteBuf,CPacketEditConfig> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,p -> p.fileID,
            ByteBufCodecs.STRING_UTF8,p -> p.option,
            ByteBufCodecs.STRING_UTF8,p -> p.input,
            CPacketEditConfig::new);
    public static final Handler<CPacketEditConfig> HANDLER = new H();

    private final ResourceLocation fileID;
    private final String option;
    private final String input;

    public CPacketEditConfig(ResourceLocation fileID, String option, String input) {
        super(TYPE);
        this.fileID = fileID;
        this.option = option;
        this.input = input;
    }

    private static class H extends Handler<CPacketEditConfig>
    {

        private H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(CPacketEditConfig message, IPayloadContext context, Player player) {

            ConfigFile file = ConfigFile.lookupFile(message.fileID);
            if(file != null && !file.isClientOnly() && player.hasPermissions(2))
            {
                Map<String, ConfigOption<?>> optionMap = file.getAllOptions();
                ConfigOption<?> option = optionMap.get(message.option);
                if(option != null)
                {
                    LightmansCurrency.LogInfo(player.getName().getString() + " changed " + file.getFileID() + " -> " + message.option + " to " + message.input);
                    option.load(message.input,ConfigOption.LoadSource.COMMAND);
                }
                else
                    LightmansCurrency.LogWarning("Failed to load config option edit packet!");
            }
            if(file != null && file.isClientOnly())
                LightmansCurrency.LogWarning("Attempted to change a client-only config on the server!");
        }

    }

}
