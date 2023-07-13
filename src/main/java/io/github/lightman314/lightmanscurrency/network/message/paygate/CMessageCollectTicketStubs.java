package io.github.lightman314.lightmanscurrency.network.message.paygate;

import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CMessageCollectTicketStubs {

    private final long traderID;
    public CMessageCollectTicketStubs(long traderID) { this.traderID = traderID; }

    public static void encode(CMessageCollectTicketStubs message, FriendlyByteBuf buffer) { buffer.writeLong(message.traderID); }
    public static CMessageCollectTicketStubs decode(FriendlyByteBuf buffer) { return new CMessageCollectTicketStubs(buffer.readLong()); }

    public static void handle(CMessageCollectTicketStubs message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() -> {
            if(TraderSaveData.GetTrader(false, message.traderID) instanceof PaygateTraderData paygate)
                paygate.collectTicketStubs(supplier.get().getSender());
        });
        supplier.get().setPacketHandled(true);
    }

}
