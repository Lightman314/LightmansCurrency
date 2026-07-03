package io.github.lightman314.lightmanscurrency.api.trader.trade.data;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.RegistryHelper;

public record TradeDataType<T extends TradeData>(MapCodec<T> codec) {

    @Override
    public String toString() { return RegistryHelper.toString("TradeDataType",LCRegistries.Trader.TRADE_DATA_TYPE,this); }

}