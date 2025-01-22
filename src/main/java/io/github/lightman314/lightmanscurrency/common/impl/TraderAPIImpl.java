package io.github.lightman314.lightmanscurrency.common.impl;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITradeSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITraderSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.data.types.TraderDataCache;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraderAPIImpl extends TraderAPI {

    public static final TraderAPIImpl INSTANCE = new TraderAPIImpl();

    private final Map<String, TraderType<?>> traderRegistry = new HashMap<>();
    private final Map<String, TradeRuleType<?>> tradeRuleRegistry = new HashMap<>();

    private final List<ITraderSearchFilter> traderSearchFilters = new ArrayList<>();
    private final List<ITradeSearchFilter> tradeSearchFilters = new ArrayList<>();

    private TraderAPIImpl() {}

    @Override
    public void RegisterTrader(@Nonnull TraderType<?> type) {
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
    public TraderType<?> GetTraderType(@Nonnull ResourceLocation type) { return this.traderRegistry.get(type.toString()); }

    @Override
    public void RegisterTradeRule(@Nonnull TradeRuleType<?> type) {
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
    public TradeRuleType<?> GetTradeRuleType(@Nonnull ResourceLocation type) { return this.tradeRuleRegistry.get(type.toString()); }

    @Nonnull
    @Override
    public List<TradeRuleType<?>> GetAllTradeRuleTypes() { return ImmutableList.copyOf(this.tradeRuleRegistry.values()); }

    @Override
    public void RegisterTraderSearchFilter(@Nonnull ITraderSearchFilter filter) {
        if(this.traderSearchFilters.contains(filter))
            return;
        this.traderSearchFilters.add(filter);
    }

    @Override
    public boolean FilterTrader(@Nonnull TraderData data, @Nonnull String searchText) {
        if(searchText.isBlank())
            return true;
        for(ITraderSearchFilter filter : this.traderSearchFilters)
        {
            if(filter.filter(data,searchText,data.registryAccess()))
                return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public List<TraderData> FilterTraders(@Nonnull List<TraderData> data, @Nonnull String searchText) {
        if(searchText.isBlank())
            return data;
        List<TraderData> results = new ArrayList<>();
        for(TraderData trader : data)
        {
            if(this.FilterTrader(trader, searchText))
                results.add(trader);
        }
        return results;
    }

    @Override
    public void RegisterTradeSearchFilter(@Nonnull ITradeSearchFilter filter) {
        if(this.tradeSearchFilters.contains(filter))
            return;
        this.tradeSearchFilters.add(filter);
    }

    @Override
    public boolean FilterTrade(@Nonnull TradeData trade, @Nonnull String searchText, @Nonnull RegistryAccess registryAccess) {
        if(searchText.isBlank())
            return true;
        for(ITradeSearchFilter filter : this.tradeSearchFilters)
        {
            if(filter.filterTrade(trade,searchText,registryAccess))
                return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public List<TradeData> FilterTrades(@Nonnull List<TradeData> trades, @Nonnull String searchText, @Nonnull RegistryAccess registryAccess) {
        if(searchText.isBlank())
            return trades;
        List<TradeData> result = new ArrayList<>();
        for(TradeData trade : trades)
        {
            if(this.FilterTrade(trade,searchText,registryAccess))
                result.add(trade);
        }
        return result;
    }

    @Override
    public <T extends ITraderSearchFilter & ITradeSearchFilter> void RegisterSearchFilter(@Nonnull T filter) {
        this.RegisterTraderSearchFilter(filter);
        this.RegisterTradeSearchFilter(filter);
    }

    @Nullable
    @Override
    public TraderData GetTrader(boolean isClient, long traderID) {
        TraderDataCache data = TraderDataCache.TYPE.get(isClient);
        if(data == null)
            return null;
        return data.getTrader(traderID);
    }

    @Nonnull
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
    public long CreateTrader(@Nonnull TraderData newTrader, @Nullable Player player) {
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
