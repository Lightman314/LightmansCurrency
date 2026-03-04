package io.github.lightman314.lightmanscurrency.network.message.player;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class CPacketRequestName extends ClientToServerPacket {

    private static final Type<CPacketRequestName> TYPE = cType("player_name_request");
    private static final StreamCodec<ByteBuf,CPacketRequestName> STREAM_CODEC = UUIDUtil.STREAM_CODEC
            .map(CPacketRequestName::new,p -> p.playerID);
    public static final Handler<CPacketRequestName> HANDLER = new H();

    private final UUID playerID;

    public CPacketRequestName(UUID playerID) {
        super(TYPE);
        this.playerID = playerID;
    }

    private static class H extends Handler<CPacketRequestName>
    {

        protected H() { super(TYPE,STREAM_CODEC); }

        @Override
        protected void handle(CPacketRequestName message, IPayloadContext context, Player player) {
            String name = PlayerReference.getPlayerName(message.playerID);
            if(name != null)
                context.reply(new SPacketUpdatePlayerCache(message.playerID,name));
        }

    }

}
