package io.github.lightman314.lightmanscurrency.network.message.player;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class CPacketRequestName extends ClientToServerPacket {

    public static final Handler<CPacketRequestName> HANDLER = new H();

    private final UUID playerID;
    public CPacketRequestName(@Nonnull UUID playerID) { this.playerID = playerID; }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer) {
        buffer.writeUUID(this.playerID);
    }

    private static class H extends Handler<CPacketRequestName>
    {
        @Nonnull
        @Override
        public CPacketRequestName decode(@Nonnull FriendlyByteBuf buffer) {
            return new CPacketRequestName(buffer.readUUID());
        }

        @Override
        protected void handle(@Nonnull CPacketRequestName message, @Nullable ServerPlayer sender) {
            String name = PlayerReference.getPlayerName(message.playerID);
            if(name != null)
            {
                //Get proper name as capitalization may be different
                new SPacketUpdatePlayerCache(message.playerID,name).sendTo(sender);
            }
        }
    }

}
