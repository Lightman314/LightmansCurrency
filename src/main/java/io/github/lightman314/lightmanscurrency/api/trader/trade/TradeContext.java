package io.github.lightman314.lightmanscurrency.api.trader.trade;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.trader.TraderAdminSettings;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.event.TraderEvent;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceSource;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceWrapper;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.*;

public final class TradeContext implements AutoCloseable,INodeAccess,ISidedContext,IRegistryAccess {

    private final Transaction transaction;
    public TransactionContext getTransaction() { return this.transaction; }
    public Transaction openNewTransaction() { return Transaction.open(this.transaction); }

    @Nullable
    private final Level level;
    @Nullable
    public Level getLevel() { return this.level; }
    @Override
    public boolean isClient() {
        if(this.level == null)
            return this.trader.isClient();
        return this.level.isClientSide();
    }
    private final HolderLookup.Provider registryAccess;
    @Override
    public HolderLookup.Provider registryAccess() { return this.registryAccess; }

    @Nullable
    private final TraderData trader;
    @Nullable
    public TraderData getTrader() { return this.trader; }
    @Nullable
    public <T extends TraderNode> T getNode(TraderNodeType<T> type) {
        if(this.trader == null)
            return null;
        return this.trader.getNode(type);
    }
    public boolean hasNode(TraderNodeType<?> type) {
        if(this.trader == null)
            return false;
        return this.trader.hasNode(type);
    }
    public <T extends TraderNode> T getNodeOrThrow(TraderNodeType<T> type) throws TradeFailedException
    {
        T node = this.getNode(type);
        if(node == null)
            throw new TradeFailedException(type);
        return node;
    }
    @Override
    public List<TraderNode> getAllNodes() { return this.trader.getAllNodes(); }

    private final Map<Identifier,List<String>> flags = new HashMap<>();
    public void addFlag(Identifier key,String flag) { this.flags.computeIfAbsent(key,k -> new ArrayList<>()).add(flag); }
    public boolean hasFlag(Identifier key,String flag) { return this.flags.getOrDefault(key,List.of()).contains(flag); }
    public boolean consumeFlag(Identifier key,String flag) { return this.flags.getOrDefault(key,new ArrayList<>()).remove(flag); }

    private TraderAdminSettings adminSettings = null;
    public TraderAdminSettings getAdminSettings() {
        if(this.adminSettings == null)
            this.adminSettings = TraderAdminSettings.collect(this);
        return this.adminSettings;
    }

    private final RandomSource random;
    public RandomSource getRandom() { return this.random; }

    private final TradeCustomer customer;
    public TradeCustomer getCustomer() { return this.customer; }
    public PlayerReference getPlayer() { return this.customer.asReference(); }
    public boolean isEditingView() { return this.customer.isEditing(); }

    private final boolean networkAccess;
    public boolean isNetworkAccess() { return this.networkAccess; }

    private final Map<ResourceType<?,?>,Object> traderResources;
    private final Map<ResourceType<?,?>,Object> customerResources;
    public <T> T getCustomerResource(ResourceType<?,T> type) { return this.getResource(ResourceSource.CUSTOMER,type); }
    public <T> T getTraderResource(ResourceType<?,T> type) { return this.getResource(ResourceSource.TRADER,type); }
    public boolean hasCustomerResource(ResourceType<?,?> type) { return this.hasResource(ResourceSource.CUSTOMER,type); }
    public boolean hasTraderResource(ResourceType<?,?> type) { return this.hasResource(ResourceSource.TRADER,type); }
    private <T> T getResource(ResourceSource source,ResourceType<?,T> type)
    {
        Map<ResourceType<?,?>,Object> map = source == ResourceSource.TRADER ? this.traderResources : this.customerResources;
        if(map.containsKey(type))
        {
            try { return (T)map.get(type);
            } catch (ClassCastException e) { LightmansCurrency.LogError("Error attempting to obtain a resource!",e); }
        }
        LightmansCurrency.LogDebug("Attempted to get a resource (" + type.getType() + ") that is not present in this trade context");
        return type.empty();
    }
    private boolean hasResource(ResourceSource source,ResourceType<?,?> type) {
        Map<ResourceType<?,?>,Object> map = source == ResourceSource.TRADER ? this.traderResources : this.customerResources;
        return map.containsKey(type);
    }

    private TradeContext(Builder builder,@Nullable TransactionContext transaction)
    {
        this.trader = builder.trader;
        this.level = builder.level;
        this.registryAccess = builder.registryAccess;
        this.customer = builder.customer;
        this.random = builder.random;
        this.networkAccess = builder.networkAccess;
        this.traderResources = combineResources(builder.traderResources,builder.wrappers);
        this.customerResources = combineResources(builder.customerResources,builder.wrappers);
        this.transaction = Transaction.open(transaction);
    }

    private Map<ResourceType<?,?>,Object> combineResources(Map<ResourceType<?,?>,List<?>> resources,Map<ResourceType<?,?>,List<ResourceWrapper<?,?>>> wrapperMap)
    {
        Map<ResourceType<?,?>,Object> builder = new HashMap<>();
        Map<ResourceType<?,?>,List<?>> copy = new HashMap<>(resources);
        //Go through the wrappers first
        wrapperMap.forEach((original,wrappers) -> {
            List<?> originalList = copy.computeIfAbsent(original,r -> new ArrayList<>());
            Object result = original.tryCombine(originalList);
            if(result == null)
                LightmansCurrency.LogError("Resource of type " + original.getType() + " returned a null result!");
            else {
                builder.put(original,result);
                copy.remove(original);
                for(ResourceWrapper<?,?> wrapper : wrappers) {
                    ResourceType<?,?> added = wrapper.getResultingResource();
                    addResource(copy,result,wrapper);
                }
            }
        });
        copy.forEach((type,list) -> {
            Object result = type.tryCombine(list);
            if(result == null)
                LightmansCurrency.LogError("Resource of type " + type.getType() + " returned a null result!");
            else
                builder.put(type,result);
        });
        return Map.copyOf(builder);
    }

    private <T> void addResource(Map<ResourceType<?,?>,List<?>> datamap, Object originalResult, ResourceWrapper<?,T> wrapper) {
        Builder.addResource(datamap,wrapper.getResultingResource(),wrapper.wrapResourceUnsafe(this,originalResult));
    }


    public static Builder dummyBuilder(Level level,HolderLookup.Provider registryAccess) { return builder(null,level,registryAccess,TradeCustomer.EDITING,RandomSource.create(-1),false); }
    public static Builder builder(TraderData trader,@Nullable Level level) { return builder(Objects.requireNonNull(trader),level,trader.registryAccess(),TradeCustomer.EDITING,RandomSource.create(-1),false); }
    public static Builder builder(TraderData trader,@Nullable Level level,TradeCustomer customer,RandomSource random, boolean networkAccess) { return builder(Objects.requireNonNull(trader),level,trader.registryAccess(),customer,random,networkAccess); }
    private static Builder builder(@Nullable TraderData trader,@Nullable Level level,HolderLookup.Provider registryAccess,TradeCustomer customer,RandomSource random,boolean networkAccess) {
        return new Builder(trader,level,registryAccess,customer,random,networkAccess);
    }

    @Override
    public void close() { this.transaction.close(); }

    public void commit() {
        if(this.customer.isEditing())
            this.close();
        this.transaction.commit();
    }

    public static final class Builder implements ISidedContext, IRegistryAccess
    {

        @Nullable
        private final TraderData trader;
        private final Level level;
        private final HolderLookup.Provider registryAccess;
        private final TradeCustomer customer;
        private final RandomSource random;
        private final boolean networkAccess;

        private final Map<ResourceType<?,?>,List<?>> traderResources = new HashMap<>();
        private final Map<ResourceType<?,?>,List<?>> customerResources = new HashMap<>();

        private final Map<ResourceType<?,?>,List<ResourceWrapper<?,?>>> wrappers = new HashMap<>();

        private Builder(@Nullable TraderData trader,Level level,HolderLookup.Provider registryAccess,TradeCustomer customer,RandomSource random,boolean networkAccess) {
            this.trader = trader;
            this.level = level;
            this.registryAccess = registryAccess;
            this.customer = customer;
            this.random = random;
            this.networkAccess = networkAccess;
            //Post-init collections
            if(this.trader != null) {
                this.trader.collectResources(this::addTraderResource);
                //Post the Resource Wrapper event
                NeoForge.EVENT_BUS.post(new TraderEvent.TraderResourceWrapperEvent(this.trader,this));
            }
        }

        @Override
        public boolean isClient() {
            if(this.level == null)
                return this.trader.isClient();
            return this.level.isClientSide();
        }

        @Override
        public HolderLookup.Provider registryAccess() { return this.registryAccess; }

        private <T> void addTraderResource(ResourceType<T,?> type, T resource) { addResource(this.traderResources,type,resource); }

        private static <T> void addResource(Map<ResourceType<?,?>,List<?>> map,ResourceType<T,?> type,T resource)
        {
            try {
                List<T> list = (List<T>)map.computeIfAbsent(type,t -> new ArrayList<T>());
                list.add(resource);
            } catch (ClassCastException e) { LightmansCurrency.LogError("Error attempting to add a resource!",e); }
        }

        public <T> Builder withResource(ResourceType<T,?> type,T resource) { addResource(this.customerResources,type,resource); return this; }

        public Builder withResourceWrapper(ResourceWrapper<?,?> wrapper) { this.wrappers.computeIfAbsent(wrapper.getWrappedResource(),r -> new ArrayList<>()).add(wrapper); return this; }

        public TradeContext build() { return this.build(null); }
        public TradeContext build(@Nullable TransactionContext transaction) { return new TradeContext(this,transaction); }

    }

}