package io.github.lightman314.lightmanscurrency.network.message.paygate;

import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class CPacketCollectTicketStubs extends ClientToServerPacket {

    public static final Handler<CPacketCollectTicketStubs> HANDLER = new H();

    private final long traderID;
    public CPacketCollectTicketStubs(long traderID) { this.traderID = traderID; }

    public void encode(@Nonnull FriendlyByteBuf buffer) { buffer.writeLong(this.traderID); }

    private static class H extends Handler<CPacketCollectTicketStubs>
    {
        @Nonnull
        @Override
        public CPacketCollectTicketStubs decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketCollectTicketStubs(buffer.readLong()); }
        @Override
        protected void handle(@Nonnull CPacketCollectTicketStubs message, @Nullable ServerPlayer sender) {
            if(TraderSaveData.GetTrader(false, message.traderID) instanceof PaygateTraderData paygate && sender != null)
                paygate.collectTicketStubs(sender);
        }
    }

}
