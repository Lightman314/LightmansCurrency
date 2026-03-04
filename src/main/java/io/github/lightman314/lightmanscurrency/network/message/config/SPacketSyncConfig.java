package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public class SPacketSyncConfig extends ServerToClientPacket {

    private static final Type<SPacketSyncConfig> TYPE = sType("config_sync");
    private static final StreamCodec<ByteBuf,SPacketSyncConfig> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,p -> p.fileID,
            ByteBufCodecs.map(HashMap::new,ByteBufCodecs.STRING_UTF8,ByteBufCodecs.STRING_UTF8),p -> p.data,
            SPacketSyncConfig::new);
    public static final Handler<SPacketSyncConfig> HANDLER = new H();

    private final ResourceLocation fileID;
    private final Map<String,String> data;

    public SPacketSyncConfig(ResourceLocation fileID, Map<String,String> data) {
        super(TYPE);
        this.fileID = fileID;
        this.data = data;
    }

    private static class H extends Handler<SPacketSyncConfig>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(SPacketSyncConfig message, IPayloadContext context, Player player) {
            ConfigFile.handleSyncData(message.fileID, message.data);
        }
    }

}
