package io.github.lightman314.lightmanscurrency.api.trader.trade.data;

import com.mojang.serialization.MapCodec;

public record TradeDataType<T extends TradeData>(MapCodec<T> codec) {

}