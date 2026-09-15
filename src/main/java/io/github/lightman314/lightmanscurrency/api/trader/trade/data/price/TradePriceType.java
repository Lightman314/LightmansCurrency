package io.github.lightman314.lightmanscurrency.api.trader.trade.data.price;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public final class TradePriceType<T extends TradePrice> extends AbstractType.Serializable<T,TradePriceType<?>> {

    private final Supplier<T> factory;
    public Supplier<T> factory() { return this.factory; }
    public T create() { return this.factory.get(); }
    public TradePriceType(MapCodec<T> codec,StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec,Supplier<T> factory) {
        super(codec, streamCodec);
        this.factory = factory;
    }

    @Override
    protected Registry<TradePriceType<?>> getRegistry() { return LCRegistries.Trader.TRADE_PRICE_TYPE; }
    @Override
    protected String getName() { return "TradePriceType"; }

}