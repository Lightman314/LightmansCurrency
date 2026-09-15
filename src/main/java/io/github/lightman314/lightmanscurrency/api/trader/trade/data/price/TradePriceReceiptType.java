package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class TradePriceReceiptType<T extends TradePriceReceipt> extends AbstractType.Serializable<T,TradePriceReceiptType<?>> {

    public TradePriceReceiptType(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) { super(codec, streamCodec); }

    @Override
    protected Registry<TradePriceReceiptType<?>> getRegistry() { return LCRegistries.Trader.TRADE_PRICE_RECEIPT_TYPE; }
    @Override
    protected String getName() { return "TradePriceReceiptType"; }

}