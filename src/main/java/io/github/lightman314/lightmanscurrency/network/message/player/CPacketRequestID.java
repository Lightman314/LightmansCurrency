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
public class CPacketRequestID extends ClientToServerPacket {

    public static final Handler<CPacketRequestID> HANDLER = new H();

    private final String playerName;
    public CPacketRequestID(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.playerName);
    }

    private static class H extends Handler<CPacketRequestID>
    {
        @Override
        public CPacketRequestID decode(FriendlyByteBuf buffer) {
            return new CPacketRequestID(buffer.readUtf());
        }

        @Override
        protected void handle(CPacketRequestID message, Player player) {
            UUID id = PlayerReference.getPlayerID(message.playerName);
            if(id != null)
            {
                //Get proper name as capitalization may be different
                String name = PlayerReference.getPlayerName(id);
                new SPacketUpdatePlayerCache(id,name == null ? message.playerName : name).sendTo(player);
            }
        }
    }

}
