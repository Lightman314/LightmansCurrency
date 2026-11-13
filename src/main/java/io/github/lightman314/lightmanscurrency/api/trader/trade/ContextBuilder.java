package io.github.lightman314.lightmanscurrency.api.trader.trade;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;

public interface ContextBuilder {

    ContextBuilder NULL = t -> TradeContext.builder(t,null);

    TradeContext.Builder buildTradeContext(TraderData trader);

}