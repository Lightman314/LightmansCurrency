package io.github.lightman314.lightmanscurrency.network.message.paygate;

import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketCollectTicketStubs extends ClientToServerPacket {

    public static final Handler<CPacketCollectTicketStubs> HANDLER = new H();

    private final long traderID;
    public CPacketCollectTicketStubs(long traderID) { this.traderID = traderID; }

    public void encode(FriendlyByteBuf buffer) { buffer.writeLong(this.traderID); }

    private static class H extends Handler<CPacketCollectTicketStubs>
    {
        @Override
        public CPacketCollectTicketStubs decode(FriendlyByteBuf buffer) { return new CPacketCollectTicketStubs(buffer.readLong()); }
        @Override
        protected void handle(CPacketCollectTicketStubs message, Player player) {
            if(TraderAPI.getApi().GetTrader(false, message.traderID) instanceof PaygateTraderData paygate)
                paygate.collectTicketStubs(player);
        }
    }

}
