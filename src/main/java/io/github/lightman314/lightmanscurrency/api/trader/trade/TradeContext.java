package io.github.lightman314.lightmanscurrency.api.trader.trade;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceSource;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceType;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TradeContext implements AutoCloseable, INodeAccess {

    private final Transaction transaction;
    public TransactionContext getTransaction() { return this.transaction; }

    private final TraderData trader;
    public TraderData getTrader() { return this.trader; }
    @Nullable
    public <T extends TraderNode> T getNode(TraderNodeType<T> type) { return this.trader.getNode(type); }
    public boolean hasNode(TraderNodeType<?> type) { return this.trader.hasNode(type); }
    public <T extends TraderNode> T getNodeOrThrow(TraderNodeType<T> type) throws TradeFailedException
    {
        T node = this.getNode(type);
        if(node == null)
            throw new TradeFailedException(type);
        return node;
    }
    @Override
    public List<TraderNode> getAllNodes() { return this.trader.getAllNodes(); }

    private final TradeCustomer customer;
    public TradeCustomer getCustomer() { return this.customer; }
    public PlayerReference getPlayer() { return this.customer.getPlayer(); }

    private final Map<ResourceType<?,?>,Object> traderResources;
    private final Map<ResourceType<?,?>,Object> customerResources;
    public <T> T getResource(ResourceSource source,ResourceType<?,T> type)
    {
        Map<ResourceType<?,?>,Object> map = source == ResourceSource.TRADER ? this.traderResources : this.customerResources;
        if(map.containsKey(type))
        {
            try { return (T)map.get(type);
            } catch (ClassCastException e) { LightmansCurrency.LogError("Error attempting to obtain a resource!",e); }
        }
        return type.empty();
    }

    private TradeContext(Builder builder,@Nullable TransactionContext transaction)
    {
        this.trader = builder.trader;
        this.customer = builder.customer;
        this.traderResources = combineResources(builder.traderResources);
        this.customerResources = combineResources(builder.customerResources);
        this.transaction = Transaction.open(transaction);
    }

    private static ImmutableMap<ResourceType<?,?>,Object> combineResources(Map<ResourceType<?,?>,List<?>> resources)
    {
        ImmutableMap.Builder<ResourceType<?,?>,Object> builder = ImmutableMap.builderWithExpectedSize(resources.size());
        resources.forEach((type,list) -> builder.put(type,type.tryCombine(list)));
        return builder.build();
    }

    public static Builder builder(TraderData trader) { return builder(trader,TradeCustomer.DISPLAY); }
    public static Builder builder(TraderData trader,TradeCustomer customer) { return new Builder(trader,customer); }

    @Override
    public void close() { this.transaction.close(); }

    public void commit() {
        if(this.customer.isDisplay())
            this.close();
        this.transaction.commit();
    }

    public static class Builder
    {

        private final TraderData trader;
        private final TradeCustomer customer;

        private final Map<ResourceType<?,?>,List<?>> traderResources = new HashMap<>();
        private final Map<ResourceType<?,?>,List<?>> customerResources = new HashMap<>();

        private Builder(TraderData trader,TradeCustomer customer) {
            this.trader = trader;
            this.customer = customer;
            this.trader.collectResources(this::addTraderResource);
        }

        private <T> void addTraderResource(ResourceType<T,?> type,T resource) { this.addResource(this.traderResources,type,resource); }

        private <T> void addResource(Map<ResourceType<?,?>,List<?>> map,ResourceType<T,?> type,T resource)
        {
            try {
                List<T> list = (List<T>)this.customerResources.computeIfAbsent(type,t -> new ArrayList<T>());
                list.add(resource);
            } catch (ClassCastException e) { LightmansCurrency.LogError("Error attempting to add a resource!",e); }
        }

        public <T> Builder withResource(ResourceType<T,?> type,T resource) { this.addResource(this.customerResources,type,resource); return this; }

        public TradeContext build() { return this.build(null); }
        public TradeContext build(@Nullable TransactionContext transaction) { return new TradeContext(this,transaction); }

    }

}