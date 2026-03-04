package io.github.lightman314.lightmanscurrency.network.message.paygate;

import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes.TicketStubNode;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketCollectTicketStubs extends ClientToServerPacket {

    private static final Type<CPacketCollectTicketStubs> TYPE = cType("paygate_collect_tickets");
    private static final StreamCodec<ByteBuf,CPacketCollectTicketStubs> STREAM_CODEC = ByteBufCodecs.VAR_LONG
            .map(CPacketCollectTicketStubs::new,p -> p.traderID);
    public static final Handler<CPacketCollectTicketStubs> HANDLER = new H();

    private final long traderID;
    public CPacketCollectTicketStubs(long traderID) { super(TYPE); this.traderID = traderID; }

    private static class H extends Handler<CPacketCollectTicketStubs>
    {
        protected H() { super(TYPE,STREAM_CODEC); }
        @Override
        protected void handle(CPacketCollectTicketStubs message, IPayloadContext context, Player player) {
            TraderData trader = TraderAPI.getApi().GetTrader(false, message.traderID);
            if(trader != null && trader.hasNode(TicketStubNode.TYPE))
                trader.getNode(TicketStubNode.TYPE).collectTicketStubs(player);
        }
    }

}
