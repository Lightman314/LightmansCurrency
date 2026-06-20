package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.RegistryHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record TradePriceType<T extends TradePrice>(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) {

    @Override
    public int hashCode() { return RegistryHelper.hash(LCRegistries.Trader.TRADE_PRICE_TYPE,this); }

    @Override
    public String toString() { return RegistryHelper.toString("TradePriceType",LCRegistries.Trader.TRADE_PRICE_TYPE,this); }
}
