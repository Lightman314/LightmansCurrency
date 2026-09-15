package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

/**
 * A post-trade result that declares the price paid by the customer.<br>
 * Serves two main purposes.<br>
 * 1- Allows listeners of the {@link io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent.Post TradeEvent.Post}
 * event to access the full data of what was paid by the customer (for prices that are more flexible like the
 * {@link io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.ItemPrice ItemPrice}<br>
 * 2- Allows trade-related notifications to store the receipt, and
 */
public abstract class TradePriceReceipt {

    public static final Codec<TradePriceReceipt> CODEC = LCRegistries.Trader.TRADE_PRICE_RECEIPT_TYPE.byNameCodec()
            .dispatch(TradePriceReceipt::getType,TradePriceReceiptType::codec);

    public static final StreamCodec<RegistryFriendlyByteBuf,TradePriceReceipt> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Trader.TRADE_PRICE_RECEIPT_TYPE_KEY)
            .dispatch(TradePriceReceipt::getType,TradePriceReceiptType::streamCodec);


    public abstract TradePriceReceiptType<?> getType();

    public abstract Component getText();

    @Override
    public final boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(obj instanceof TradePriceReceipt other)
            return this.equals(other);
        return false;
    }

    @Override
    public final int hashCode() { return Objects.hash(this.getType(),this.hash()); }

    protected abstract boolean equals(TradePriceReceipt other);
    protected abstract int hash();

}