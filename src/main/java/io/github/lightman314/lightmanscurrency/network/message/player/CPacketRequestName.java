package io.github.lightman314.lightmanscurrency.network.message.player;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketRequestName extends ClientToServerPacket {

    public static final Handler<CPacketRequestName> HANDLER = new H();

    private final UUID playerID;
    public CPacketRequestName(UUID playerID) { this.playerID = playerID; }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.playerID);
    }

    private static class H extends Handler<CPacketRequestName>
    {
        @Override
        public CPacketRequestName decode(FriendlyByteBuf buffer) {
            return new CPacketRequestName(buffer.readUUID());
        }

        @Override
        protected void handle(CPacketRequestName message, Player player) {
            String name = PlayerReference.getPlayerName(message.playerID);
            if(name != null)
            {
                //Get proper name as capitalization may be different
                new SPacketUpdatePlayerCache(message.playerID,name).sendTo(player);
            }
        }
    }

}
