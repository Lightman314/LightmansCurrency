package io.github.lightman314.lightmanscurrency.network.message.config;

import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public class SPacketSyncConfig extends ServerToClientPacket {

    public static final Type<SPacketSyncConfig> TYPE = sType("config_sync");
    public static final StreamCodec<ByteBuf,SPacketSyncConfig> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,p -> p.fileID,
            ByteBufCodecs.map(HashMap::new,ByteBufCodecs.STRING_UTF8,ByteBufCodecs.STRING_UTF8),p -> p.data,
            SPacketSyncConfig::new);

    private final Identifier fileID;
    private final Map<String,String> data;
    public SPacketSyncConfig(Identifier fileID,Map<String,String> data) {
        super(TYPE);
        this.fileID = fileID;
        this.data = data;
    }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        ConfigFile.handleSyncData(this.fileID,this.data);
    }

}