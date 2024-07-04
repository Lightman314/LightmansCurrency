package io.github.lightman314.lightmanscurrency.network.message.paygate;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketCollectTicketStubs extends ClientToServerPacket {

    private static final Type<CPacketCollectTicketStubs> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"c_paygate_collect_tickets"));
    public static final Handler<CPacketCollectTicketStubs> HANDLER = new H();

    private final long traderID;
    public CPacketCollectTicketStubs(long traderID) { super(TYPE); this.traderID = traderID; }

    private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketCollectTicketStubs message) { buffer.writeLong(message.traderID); }
    private static CPacketCollectTicketStubs decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketCollectTicketStubs(buffer.readLong()); }

    private static class H extends Handler<CPacketCollectTicketStubs>
    {
        protected H() { super(TYPE, easyCodec(CPacketCollectTicketStubs::encode,CPacketCollectTicketStubs::decode)); }
        @Override
        protected void handle(@Nonnull CPacketCollectTicketStubs message, @Nonnull IPayloadContext context, @Nonnull Player player) {
            if(TraderSaveData.GetTrader(false, message.traderID) instanceof PaygateTraderData paygate)
                paygate.collectTicketStubs(player);
        }
    }

}
