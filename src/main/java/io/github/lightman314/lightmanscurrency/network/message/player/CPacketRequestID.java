package io.github.lightman314.lightmanscurrency.network.message.player;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class CPacketRequestID extends ClientToServerPacket {

    public static final Handler<CPacketRequestID> HANDLER = new H();

    private final String playerName;
    public CPacketRequestID(@Nonnull String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer) {
        buffer.writeUtf(this.playerName);
    }

    private static class H extends Handler<CPacketRequestID>
    {
        @Nonnull
        @Override
        public CPacketRequestID decode(@Nonnull FriendlyByteBuf buffer) {
            return new CPacketRequestID(buffer.readUtf());
        }

        @Override
        protected void handle(@Nonnull CPacketRequestID message, @Nullable ServerPlayer sender) {
            UUID id = PlayerReference.getPlayerID(message.playerName);
            if(id != null)
            {
                //Get proper name as capitalization may be different
                String name = PlayerReference.getPlayerName(id);
                new SPacketUpdatePlayerCache(id,name == null ? message.playerName : name).sendTo(sender);
            }
        }
    }

}
