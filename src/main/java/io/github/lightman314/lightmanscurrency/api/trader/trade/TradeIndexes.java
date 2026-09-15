package io.github.lightman314.lightmanscurrency.api.trader.trade;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public record TradeIndexes(int traderIndex, int nodeIndex, int tradeIndex) {

    @Nullable
    public TraderData getTrader(TraderSource source) {
        List<TraderData> traders = source.getTraders();
        if(this.traderIndex < 0 || this.traderIndex >= traders.size())
            return null;
        return traders.get(this.traderIndex);
    }

    @Nullable
    public TradingNode<?> getNode(TraderSource source) {
        TraderData trader = this.getTrader(source);
        if(trader == null)
            return null;
        List<TradingNode<?>> nodes = trader.getTradingNodes();
        if(this.nodeIndex < 0 || this.nodeIndex >= nodes.size())
            return null;
        return nodes.get(this.nodeIndex);
    }

    @Nullable
    public TradeData getTrade(TraderSource source) {
        TradingNode<?> node = this.getNode(source);
        return node == null ? null : node.getTrade(this.tradeIndex);
    }

    public static Optional<TradeIndexes> lookup(TraderSource source, TraderData trader, TradingNode<?> node, TradeData trade) {
        List<TraderData> traders = source.getTraders();
        int traderIndex = traders.indexOf(trader);
        if(traderIndex < 0)
            return Optional.empty();
        List<TradingNode<?>> nodes = trader.getTradingNodes();
        int nodeIndex = nodes.indexOf(node);
        if(nodeIndex < 0)
            return Optional.empty();
        int tradeIndex = node.getTrades().indexOf(trade);
        if(tradeIndex < 0)
            return Optional.empty();
        return Optional.of(new TradeIndexes(traderIndex,nodeIndex,tradeIndex));
    }

    public List<Integer> asData() { return List.of(this.traderIndex,this.nodeIndex,this.tradeIndex); }
    @Nullable
    public static TradeIndexes fromData(List<Integer> list) {
        if(list.size() == 3)
            return new TradeIndexes(list.get(0),list.get(1),list.get(2));
        return null;
    }

}