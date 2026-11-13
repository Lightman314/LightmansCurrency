package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.customer.TraderAccess;
import io.github.lightman314.lightmanscurrency.features.tickets.nodes.TicketStubStorageNode;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CPacketCollectTicketStubs extends ClientToServerPacket {

    public static final Type<CPacketCollectTicketStubs> TYPE = cType("collect_ticket_stubs");
    private static final CPacketCollectTicketStubs INSTANCE = new CPacketCollectTicketStubs();
    public static final StreamCodec<ByteBuf,CPacketCollectTicketStubs> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private CPacketCollectTicketStubs() { super(TYPE); }

    public static void sendToServer() { INSTANCE.send(); }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        if(player.containerMenu instanceof TraderAccess menu) {
            TraderData trader = menu.getSimpleTrader();
            if(trader != null && trader.hasNode(TicketStubStorageNode.TYPE)) {
                trader.getNode(TicketStubStorageNode.TYPE)
                        .tryGiveToPlayer(player);
            }
        }
    }

}
