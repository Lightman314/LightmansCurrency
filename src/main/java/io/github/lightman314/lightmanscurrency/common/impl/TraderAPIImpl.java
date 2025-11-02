package io.github.lightman314.lightmanscurrency.common.impl;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITradeSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITraderSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.SortTypeKey;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.data.types.TraderDataCache;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TraderAPIImpl extends TraderAPI {

    private final Map<String, TraderType<?>> traderRegistry = new HashMap<>();
    private final Map<String, TradeRuleType<?>> tradeRuleRegistry = new HashMap<>();
    private final Map<ResourceLocation, TerminalSortType> sortTypeRegistry = new HashMap<>();

    private final List<ITraderSearchFilter> traderSearchFilters = new ArrayList<>();
    private final List<ITradeSearchFilter> tradeSearchFilters = new ArrayList<>();

    public TraderAPIImpl() {}

    @Override
    public void RegisterTrader(TraderType<?> type) {
        String t = type.type.toString();
        if(this.traderRegistry.containsKey(t))
        {
            LightmansCurrency.LogWarning("Attempted to register duplicate TraderType '" + t + "'!");
            return;
        }
        this.traderRegistry.put(t, type);
        LightmansCurrency.LogInfo("Registered TraderType " + type);
    }

    @Nullable
    @Override
    public TraderType<?> GetTraderType(ResourceLocation type) { return this.traderRegistry.get(type.toString()); }

    @Override
    public void RegisterTradeRule(TradeRuleType<?> type) {
        String t = type.type.toString();
        if(this.tradeRuleRegistry.containsKey(t))
        {
            LightmansCurrency.LogWarning("Attempted to register duplicate TradeRuleType '" + type.type + "'!");
            return;
        }
        this.tradeRuleRegistry.put(t, type);
        LightmansCurrency.LogInfo("Registered TradeRuleType " + type);
    }

    @Nullable
    @Override
    public TradeRuleType<?> GetTradeRuleType(ResourceLocation type) { return this.tradeRuleRegistry.get(type.toString()); }

    @Override
    public List<TradeRuleType<?>> GetAllTradeRuleTypes() { return ImmutableList.copyOf(this.tradeRuleRegistry.values()); }

    @Override
    public void RegisterTraderSearchFilter(ITraderSearchFilter filter) {
        if(this.traderSearchFilters.contains(filter))
            return;
        this.traderSearchFilters.add(filter);
    }

    @Override
    public boolean FilterTrader(TraderData data, String search) {
        if(search.isBlank())
            return true;
        PendingSearch results = PendingSearch.of(search);
        return this.FilterTrader(data,results);
    }

    //Local private copy so that we don't have to re-process the string during the for loop of FilterTraders
    private boolean FilterTrader(TraderData data, PendingSearch search)
    {
        PendingSearch results = search.copy();
        //Check for failed filters
        for(ITraderSearchFilter filter : this.traderSearchFilters)
            filter.filter(data,results);
        return results.hasPassed();
    }

    
    @Override
    public List<TraderData> FilterTraders(List<TraderData> data, String search) {

        if(search.isBlank())
            return data;

        PendingSearch temp = PendingSearch.of(search);

        List<TraderData> results = new ArrayList<>();
        for(TraderData trader : data)
        {
            if(this.FilterTrader(trader,temp))
                results.add(trader);
        }
        return results;
    }

    @Override
    public void RegisterTradeSearchFilter(ITradeSearchFilter filter) {
        if(this.tradeSearchFilters.contains(filter))
            return;
        this.tradeSearchFilters.add(filter);
    }

    @Override
    public boolean FilterTrade(TradeData trade, String search) {
        if(search.isBlank())
            return true;
        return this.FilterTrade(trade,PendingSearch.of(search));
    }

    private boolean FilterTrade(TradeData trade, PendingSearch search)
    {
        PendingSearch results = search.copy();
        //Check for failed filters
        for(ITradeSearchFilter filter : this.tradeSearchFilters)
            filter.filterTrade(trade,results);
        return results.hasPassed();
    }

    
    @Override
    public List<TradeData> FilterTrades(List<TradeData> trades, String search) {
        if(search.isBlank())
            return trades;
        PendingSearch temp = PendingSearch.of(search);
        List<TradeData> result = new ArrayList<>();
        for(TradeData trade : trades)
        {
            if(this.FilterTrade(trade,temp))
                result.add(trade);
        }
        return result;
    }

    @Override
    public <T extends ITraderSearchFilter & ITradeSearchFilter> void RegisterSearchFilter(T filter) {
        this.RegisterTraderSearchFilter(filter);
        this.RegisterTradeSearchFilter(filter);
    }

    @Override
    public void RegisterSortType(TerminalSortType sortType) {
        Objects.requireNonNull(sortType,"Terminal Sort Type cannot be null!");
        if(this.sortTypeRegistry.containsKey(sortType.getID()))
        {
            LightmansCurrency.LogWarning("Attempted to register duplicate TerminalSortType '" + sortType.getID() + "'!");
            return;
        }
        this.sortTypeRegistry.put(sortType.getID(),sortType);
    }

    @Nullable
    @Override
    public TerminalSortType GetSortType(ResourceLocation key) { return this.sortTypeRegistry.get(key); }

    @Nullable
    @Override
    public TerminalSortType GetSortType(SortTypeKey key) {
        TerminalSortType type = this.GetSortType(key.id());
        if(type != null && key.inverted())
            return type.getInverted();
        return type;
    }

    @Override
    public List<TerminalSortType> GetAllSortTypes() {
        List<TerminalSortType> list = new ArrayList<>(this.sortTypeRegistry.values());
        list.sort(Comparator.comparingInt(TerminalSortType::sortPriority).reversed());
        List<TerminalSortType> result = new ArrayList<>();
        for(TerminalSortType type : list)
        {
            result.add(type);
            if(type.supportsInverted())
            {
                TerminalSortType inverted = type.getInverted();
                if(inverted != null)
                    result.add(inverted);
            }
        }
        return result;
    }

    @Override
    public List<SortTypeKey> GetAllSortTypeKeys() {
        List<TerminalSortType> types = GetAllSortTypes();
        return types.stream().map(TerminalSortType::getKey).toList();
    }

    @Nullable
    @Override
    public TraderData GetTrader(boolean isClient, long traderID) {
        TraderDataCache data = TraderDataCache.TYPE.get(isClient);
        if(data == null)
            return null;
        return data.getTrader(traderID);
    }

    
    @Override
    public List<TraderData> GetAllTraders(boolean isClient) {
        TraderDataCache data = TraderDataCache.TYPE.get(isClient);
        if(data == null)
            return new ArrayList<>();
        return data.getAllTraders();
    }

    @Override
    public List<TraderData> GetAllNetworkTraders(boolean isClient) {
        TraderDataCache data = TraderDataCache.TYPE.get(isClient);
        if(data == null)
            return new ArrayList<>();
        return data.getAllTerminalTraders();
    }

    @Override
    public long CreateTrader(TraderData newTrader, @Nullable Player player) {
        TraderDataCache data = TraderDataCache.TYPE.get(false);
        if(data == null)
            return -1;
        return data.registerTrader(newTrader,player);
    }

    @Override
    public void DeleteTrader(long traderID) {
        TraderDataCache data = TraderDataCache.TYPE.get(false);
        if(data == null)
            return;
        data.deleteTrader(traderID);
    }

}