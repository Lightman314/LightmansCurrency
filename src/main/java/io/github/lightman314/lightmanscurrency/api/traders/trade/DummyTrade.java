package io.github.lightman314.lightmanscurrency.api.traders.trade;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;

import javax.annotation.Nullable;

public abstract class DummyTrade extends TradeData {

    protected TraderData trader;
    public TraderData getTrader() { return this.trader; }
    public DummyTrade() {}

    public void updateTrader(TraderData trader) { this.trader = trader; }

    @Nullable
    protected <T extends TraderNode> T getNode(TraderNodeType<T> type)
    {
        if(this.trader == null)
            return null;
        return this.trader.getNode(type);
    }

}
