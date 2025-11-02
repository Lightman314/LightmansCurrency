package io.github.lightman314.lightmanscurrency.network.message.player;

import io.github.lightman314.lightmanscurrency.client.data.ClientPlayerNameCache;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SPacketUpdatePlayerCache extends ServerToClientPacket {

    public static final Handler<SPacketUpdatePlayerCache> HANDLER = new H();

    private final UUID playerID;
    private final String playerName;

    public SPacketUpdatePlayerCache(UUID playerID, String playerName)
    {
        this.playerID = playerID;
        this.playerName = playerName;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.playerID);
        buffer.writeUtf(this.playerName);
    }

    private static class H extends Handler<SPacketUpdatePlayerCache>
    {
        @Override
        public SPacketUpdatePlayerCache decode(FriendlyByteBuf buffer) {
            return new SPacketUpdatePlayerCache(buffer.readUUID(),buffer.readUtf());
        }

        @Override
        protected void handle(SPacketUpdatePlayerCache message, Player player) {
            ClientPlayerNameCache.addCacheEntry(message.playerID,message.playerName);
        }
    }


}
