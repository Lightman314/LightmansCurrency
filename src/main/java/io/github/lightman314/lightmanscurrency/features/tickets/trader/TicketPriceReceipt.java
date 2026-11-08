package io.github.lightman314.lightmanscurrency.features.tickets.trader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceReceipt;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceReceiptType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class TicketPriceReceipt extends TradePriceReceipt {

    private static final MapCodec<TicketPriceReceipt> MAP_CODEC = Codec.LONG.fieldOf("ticketID").xmap(TicketPriceReceipt::new, TicketPriceReceipt::getTicketID);
    private static final StreamCodec<ByteBuf, TicketPriceReceipt> STREAM_CODEC = ByteBufCodecs.LONG.map(TicketPriceReceipt::new, TicketPriceReceipt::getTicketID);

    public static final TradePriceReceiptType<TicketPriceReceipt> TYPE = new TradePriceReceiptType<>(MAP_CODEC,STREAM_CODEC);

    private final long ticketID;
    public long getTicketID() { return this.ticketID; }
    public TicketPriceReceipt(long ticketID) { this.ticketID = ticketID; }

    @Override
    public TradePriceReceiptType<?> getType() { return TYPE; }

    @Override
    public Component getText() {
        return null;
    }

    @Override
    protected boolean equals(TradePriceReceipt other) {
        return other instanceof TicketPriceReceipt tpr && tpr.ticketID == this.ticketID;
    }

    @Override
    protected int hash() { return Long.hashCode(this.ticketID); }
}