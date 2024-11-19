package io.github.lightman314.lightmanscurrency.network.message.player;

import io.github.lightman314.lightmanscurrency.client.data.ClientPlayerNameCache;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import java.util.UUID;

public class SPacketUpdatePlayerCache extends ServerToClientPacket {

    public static final Type<SPacketUpdatePlayerCache> TYPE = new Type<>(VersionUtil.lcResource("s_playername_update"));
    public static final Handler<SPacketUpdatePlayerCache> HANDLER = new H();

    private final UUID playerID;
    private final String playerName;

    public SPacketUpdatePlayerCache(@Nonnull UUID playerID, @Nonnull String playerName)
    {
        super(TYPE);
        this.playerID = playerID;
        this.playerName = playerName;
    }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull SPacketUpdatePlayerCache message)
    {
        buffer.writeUUID(message.playerID);
        buffer.writeUtf(message.playerName);
    }

    private static SPacketUpdatePlayerCache decode(@Nonnull FriendlyByteBuf buffer) { return new SPacketUpdatePlayerCache(buffer.readUUID(),buffer.readUtf()); }

    private static class H extends Handler<SPacketUpdatePlayerCache>
    {
        protected H() { super(TYPE, StreamCodec.of(SPacketUpdatePlayerCache::encode,SPacketUpdatePlayerCache::decode)); }
        @Override
        protected void handle(@Nonnull SPacketUpdatePlayerCache message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            ClientPlayerNameCache.addCacheEntry(message.playerID,message.playerName);
        }
    }

}
