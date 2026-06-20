package io.github.lightman314.lightmanscurrency.api.trader.trade.data;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class TradeSet {

    private final Supplier<TradingNode<?>> nodeSource;
    private final int lockedIndex;
    public TradeSet(Supplier<TraderData> traderSource,TraderNodeType<? extends TradingNode<?>> nodeType) {
        this(() -> {
            TraderData trader = traderSource.get();
            if(trader != null)
                return trader.getNode(nodeType);
            return null;
        });
    }
    public TradeSet(TradingNode<?> node) { this(() -> node); }
    public TradeSet(Supplier<TradingNode<?>> nodeSource) { this.nodeSource = nodeSource; this.lockedIndex = -1; }
    private TradeSet(TradeSet other,int lockedIndex)
    {
        this.nodeSource = other.nodeSource;
        this.lockedIndex = lockedIndex;
    }

    public TradeSet singleTrade(int index) { return new TradeSet(this,index); }

    public Component getLabel() {
        TradingNode<?> node = this.nodeSource.get();
        return node == null ? Component.literal("ERROR") : node.getSetLabel();
    }

    public boolean contains(TradeData trade) { return this.getTrades().contains(trade); }

    public List<TradeData> getTrades() {
        TradingNode<?> node = this.nodeSource.get();
        if(node == null)
            return new ArrayList<>();
        if(this.lockedIndex >= 0)
        {
            List<? extends TradeData> trades = node.getTrades();
            if(this.lockedIndex < trades.size())
                return Lists.newArrayList(trades.get(this.lockedIndex));
            return new ArrayList<>();
        }
        return new ArrayList<>(node.getTrades());
    }

    public static List<TradeData> getAllTrades(List<TradeSet> tradeSets)
    {
        List<TradeData> trades = new ArrayList<>();
        for(TradeSet set : tradeSets)
            trades.addAll(set.getTrades());
        return trades;
    }

}