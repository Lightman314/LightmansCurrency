package io.github.lightman314.lightmanscurrency.network.message.player;

import io.github.lightman314.lightmanscurrency.client.data.ClientPlayerNameCache;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class SPacketUpdatePlayerCache extends ServerToClientPacket {

    private static final Type<SPacketUpdatePlayerCache> TYPE = sType("playername_update");
    private static final StreamCodec<ByteBuf,SPacketUpdatePlayerCache> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,p -> p.playerID,
            ByteBufCodecs.STRING_UTF8,p -> p.playerName,
            SPacketUpdatePlayerCache::new);
    public static final Handler<SPacketUpdatePlayerCache> HANDLER = new H();

    private final UUID playerID;
    private final String playerName;

    public SPacketUpdatePlayerCache(UUID playerID, String playerName)
    {
        super(TYPE);
        this.playerID = playerID;
        this.playerName = playerName;
    }
    
    private static class H extends Handler<SPacketUpdatePlayerCache>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(SPacketUpdatePlayerCache message, IPayloadContext context, Player player) {
            ClientPlayerNameCache.addCacheEntry(message.playerID,message.playerName);
        }
    }

}
