package io.github.lightman314.lightmanscurrency.api.trader.trade.data;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;

public final class TradeDataType<T extends TradeData> extends AbstractType.WithCodec<T,TradeDataType<?>> {

    public TradeDataType(MapCodec<T> codec) { super(codec); }

    @Override
    protected Registry<TradeDataType<?>> getRegistry() { return LCRegistries.Trader.TRADE_DATA_TYPE; }

    @Override
    protected String getName() { return "TradeDataType"; }

}