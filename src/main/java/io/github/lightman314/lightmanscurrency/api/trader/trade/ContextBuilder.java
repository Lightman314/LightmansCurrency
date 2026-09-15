package io.github.lightman314.lightmanscurrency.api.trader.trade;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;

public interface ContextBuilder {

    static ContextBuilder NULL = TradeContext::builder;

    TradeContext.Builder buildTradeContext(TraderData trader);

}