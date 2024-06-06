package io.github.lightman314.lightmanscurrency.api.traders;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITraderSearchFilter;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TraderAPI {

    private static final Map<String,TraderType<?>> traderRegistry = new HashMap<>();
    private static final Map<String,TradeRuleType<?>> tradeRuleRegistry = new HashMap<>();

    private static final List<ITraderSearchFilter> searchFilters = new ArrayList<>();

    private TraderAPI(){}

    public static void registerTrader(@Nonnull TraderType<?> type)
    {
        String t = type.type.toString();
        if(traderRegistry.containsKey(t))
        {
            LightmansCurrency.LogWarning("Attempted to register duplicate TraderType '" + t + "'!");
            return;
        }
        traderRegistry.put(t, type);
        LightmansCurrency.LogInfo("Registered TraderType " + type);
    }

    @Nullable
    public static TraderType<?> getTraderType(@Nonnull ResourceLocation type) { return traderRegistry.get(type.toString()); }

    public static void registerTradeRule(@Nonnull TradeRuleType<?> type)
    {
        String t = type.type.toString();
        if(tradeRuleRegistry.containsKey(t))
        {
            LightmansCurrency.LogWarning("Attempted to register duplicate TradeRuleType '" + type.type + "'!");
            return;
        }
        tradeRuleRegistry.put(t, type);
        LightmansCurrency.LogInfo("Registered TradeRuleType " + type);
    }

    @Nullable
    public static TradeRuleType<?> getTradeRuleType(@Nonnull ResourceLocation type) { return tradeRuleRegistry.get(type.toString()); }

    @Nonnull
    public static List<TradeRuleType<?>> getTradeRuleTypes() { return ImmutableList.copyOf(tradeRuleRegistry.values()); }

    public static void registerSearchFilter(@Nonnull ITraderSearchFilter filter)
    {
        if(searchFilters.contains(filter))
            return;
        searchFilters.add(filter);
    }

    public static boolean filterTrader(@Nonnull TraderData data, @Nonnull String searchText)
    {
        for(ITraderSearchFilter filter : ImmutableList.copyOf(searchFilters))
        {
            try{
                if(filter.filter(data, searchText))
                    return true;
            } catch(Throwable t) { LightmansCurrency.LogError("Error filtering traders: ", t); }
        }
        return false;
    }

    @Nonnull
    public static List<TraderData> filterTraders(@Nonnull List<TraderData> data, @Nonnull String searchText)
    {
        if(searchText.isBlank())
            return data;
        List<TraderData> results = new ArrayList<>();
        for(TraderData trader : data)
        {
            if(filterTrader(trader, searchText))
                results.add(trader);
        }
        return results;
    }

    @Nullable
    public static TraderData getTrader(@Nonnull IClientTracker clientTracker, long traderID) { return getTrader(clientTracker.isClient(), traderID); }
    @Nullable
    public static TraderData getTrader(boolean isClient, long traderID) { return  TraderSaveData.GetTrader(isClient, traderID); }

    @Nonnull
    public static List<TraderData> getAllTraders(@Nonnull IClientTracker clientTracker) { return getAllTraders(clientTracker.isClient()); }
    @Nonnull
    public static List<TraderData> getAllTraders(boolean isClient) { return TraderSaveData.GetAllTraders(isClient); }

    @Nonnull
    public static List<TraderData> getAllTerminalTraders(@Nonnull IClientTracker clientTracker) { return getAllTerminalTraders(clientTracker.isClient()); }
    @Nonnull
    public static List<TraderData> getAllTerminalTraders(boolean isClient) { return getAllTraders(isClient).stream().filter(TraderData::showOnTerminal).collect(Collectors.toList()); }

    public static long addTrader(@Nonnull TraderData newTrader) { return addTrader(newTrader, null); }
    public static long addTrader(@Nonnull TraderData newTrader, @Nullable Player player) { return TraderSaveData.RegisterTrader(newTrader, player); }

    public static void deleteTrader(@Nonnull TraderData trader) { deleteTrader(trader.getID()); }
    public static void deleteTrader(long traderID) { TraderSaveData.DeleteTrader(traderID); }

}
