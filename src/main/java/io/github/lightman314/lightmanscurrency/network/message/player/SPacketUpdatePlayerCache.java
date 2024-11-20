package io.github.lightman314.lightmanscurrency.network.message.player;

import io.github.lightman314.lightmanscurrency.client.data.ClientPlayerNameCache;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class SPacketUpdatePlayerCache extends ServerToClientPacket {

    public static final Handler<SPacketUpdatePlayerCache> HANDLER = new H();

    private final UUID playerID;
    private final String playerName;

    public SPacketUpdatePlayerCache(@Nonnull UUID playerID, @Nonnull String playerName)
    {
        this.playerID = playerID;
        this.playerName = playerName;
    }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer) {
        buffer.writeUUID(this.playerID);
        buffer.writeUtf(this.playerName);
    }

    private static class H extends Handler<SPacketUpdatePlayerCache>
    {
        @Nonnull
        @Override
        public SPacketUpdatePlayerCache decode(@Nonnull FriendlyByteBuf buffer) {
            return new SPacketUpdatePlayerCache(buffer.readUUID(),buffer.readUtf());
        }

        @Override
        protected void handle(@Nonnull SPacketUpdatePlayerCache message, @Nullable ServerPlayer sender) {
            ClientPlayerNameCache.addCacheEntry(message.playerID,message.playerName);
        }
    }


}
