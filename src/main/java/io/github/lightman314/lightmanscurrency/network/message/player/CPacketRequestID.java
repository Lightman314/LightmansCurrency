package io.github.lightman314.lightmanscurrency.network.message.player;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class CPacketRequestID extends ClientToServerPacket {

    private static final Type<CPacketRequestID> TYPE = cType("player_id_request");
    private static final StreamCodec<ByteBuf,CPacketRequestID> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
            .map(CPacketRequestID::new,p -> p.playerName);
    public static final Handler<CPacketRequestID> HANDLER = new H();

    private final String playerName;

    public CPacketRequestID(String playerName) {
        super(TYPE);
        this.playerName = playerName;
    }

    private static class H extends Handler<CPacketRequestID>
    {

        protected H() { super(TYPE,STREAM_CODEC); }

        @Override
        protected void handle(CPacketRequestID message, IPayloadContext context, Player player) {
            UUID id = PlayerReference.getPlayerID(message.playerName);
            if(id != null)
            {
                //Get proper name as capitalization may be different
                String name = PlayerReference.getPlayerName(id);
                context.reply(new SPacketUpdatePlayerCache(id,name == null ? message.playerName : name));
            }
        }

    }

}
