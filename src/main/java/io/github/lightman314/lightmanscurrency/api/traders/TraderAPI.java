package io.github.lightman314.lightmanscurrency.api.traders;

import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITradeSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITraderSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.impl.TraderAPIImpl;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TraderAPI {

    public static final TraderAPI API = TraderAPIImpl.INSTANCE;

    protected TraderAPI(){}

    /**
     * Use {@link #RegisterTrader(TraderType)} instead.
     * @see #API
     */
    @Deprecated(since = "2.2.2.3")
    public static void registerTrader(@Nonnull TraderType<?> type) { API.RegisterTrader(type); }

    /**
     * Registers the given {@link TraderType}, allowing {@link TraderData} of that type to be saved & loaded from the Trader Save Data.
     */
    public abstract void RegisterTrader(@Nonnull TraderType<?> type);

    /**
     * @deprecated Use {@link #GetTraderType(ResourceLocation)} instead.
     * @see #API
     */
    @Deprecated
    @Nullable
    public static TraderType<?> getTraderType(@Nonnull ResourceLocation type) { return API.GetTraderType(type); }

    /**
     * Accesses the {@link TraderType} registry and gets the {@link TraderType} for the given id.
     * @see #RegisterTrader(TraderType)
     */
    @Nullable
    public abstract TraderType<?> GetTraderType(@Nonnull ResourceLocation type);

    /**
     * @deprecated Use {@link #RegisterTradeRule(TradeRuleType)} instead.
     * @see #API
     */
    @Deprecated
    public static void registerTradeRule(@Nonnull TradeRuleType<?> type) { API.RegisterTradeRule(type); }

    /**
     * Registers the given {@link TradeRuleType} allowing {@link io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule Trade Rule}'s of that type to be added, saved, and loaded from any relevant trades/traders.
     */
    public abstract void RegisterTradeRule(@Nonnull TradeRuleType<?> type);

    /**
     * @deprecated Use {@link #GetTradeRuleType(ResourceLocation)} instead.
     * @see #API
     */
    @Deprecated
    @Nullable
    public static TradeRuleType<?> getTradeRuleType(@Nonnull ResourceLocation type) { return API.GetTradeRuleType(type); }

    /**
     * Accesses the {@link TradeRuleType} registry and gets the {@link TradeRuleType} for the given id.
     * @see #RegisterTradeRule(TradeRuleType)
     */
    @Nullable
    public abstract TradeRuleType<?> GetTradeRuleType(@Nonnull ResourceLocation type);

    /**
     * @deprecated Use {@link #GetAllTradeRuleTypes()} instead.
     * @see #API
     */
    @Deprecated
    @Nonnull
    public static List<TradeRuleType<?>> getTradeRuleTypes() { return API.GetAllTradeRuleTypes(); }

    /**
     * Returns an Immutable list of all registered {@link TradeRuleType}'s<br>
     * @see #RegisterTradeRule(TradeRuleType)
     */
    @Nonnull
    public abstract List<TradeRuleType<?>> GetAllTradeRuleTypes();


    /**
     * @deprecated Use {@link #RegisterTraderSearchFilter(ITraderSearchFilter)} instead.
     * @see #API
     */
    @Deprecated
    public static void registerSearchFilter(@Nonnull ITraderSearchFilter filter) { API.RegisterTraderSearchFilter(filter); }

    /**
     * Registers the given {@link ITraderSearchFilter}, allowing the ability to search traders via {@link #FilterTrader(TraderData, String)} & {@link #FilterTraders(List, String)}
     */
    public abstract void RegisterTraderSearchFilter(@Nonnull ITraderSearchFilter filter);

    /**
     * @deprecated Use {@link #FilterTrader(TraderData,String)} instead.
     * @see #API
     */
    @Deprecated
    public static boolean filterTrader(@Nonnull TraderData data, @Nonnull String searchText) { return API.FilterTrader(data,searchText); }

    /**
     * Whether the given trader matches the given search text, and should be listed in the search results
     * @see #RegisterTraderSearchFilter(ITraderSearchFilter)
     */
    public abstract boolean FilterTrader(@Nonnull TraderData data, @Nonnull String searchText);

    /**
     * @deprecated Use {@link #FilterTraders(List,String)} instead.
     * @see #API
     */
    @Deprecated
    @Nonnull
    public static List<TraderData> filterTraders(@Nonnull List<TraderData> data, @Nonnull String searchText) { return API.FilterTraders(data,searchText); }

    /**
     * Filters the given list via {@link #FilterTrader(TraderData, String)} and returns the list of traders that have passed
     */
    @Nonnull
    public abstract List<TraderData> FilterTraders(@Nonnull List<TraderData> data, @Nonnull String searchText);

    /**
     * Registers the given {@link ITradeSearchFilter}, allowing the ability to search traders via {@link #FilterTrade(TradeData,String,RegistryAccess)} & {@link #FilterTrades(List,String,RegistryAccess)}
     */
    public abstract void RegisterTradeSearchFilter(@Nonnull ITradeSearchFilter filter);
    /**
     * Whether the given trade matches the given search text, and should be listed in the search results
     * @see #RegisterTradeSearchFilter(ITradeSearchFilter)
     */
    public abstract boolean FilterTrade(@Nonnull TradeData trade, @Nonnull String searchText, @Nonnull RegistryAccess registryAccess);
    /**
     * Filters the given list via {@link #FilterTrade(TradeData, String,RegistryAccess)} and returns the list of trades that have passed
     */
    @Nonnull
    public abstract List<TradeData> FilterTrades(@Nonnull List<TradeData> trades, @Nonnull String searchText, @Nonnull RegistryAccess registryAccess);

    /**
     *  Registers the given {@link ITraderSearchFilter} & {@link ITradeSearchFilter} so that the trader & its trades can be filtered.
     * @see #RegisterTraderSearchFilter(ITraderSearchFilter)
     * @see #RegisterTradeSearchFilter(ITradeSearchFilter)
     */
    public abstract <T extends ITraderSearchFilter & ITradeSearchFilter> void RegisterSearchFilter(@Nonnull T filter);

    /**
     * @deprecated Use {@link #GetTrader(IClientTracker, long)} instead.
     * @see #GetTrader(boolean, long)
     * @see #API
     */
    @Deprecated
    @Nullable
    public static TraderData getTrader(@Nonnull IClientTracker clientTracker, long traderID) { return API.GetTrader(clientTracker,traderID); }
    /**
     * @deprecated Use {@link #GetTrader(boolean, long)} instead.
     * @see #GetTrader(IClientTracker, long)
     * @see #API
     */
    @Deprecated
    @Nullable
    public static TraderData getTrader(boolean isClient, long traderID) { return API.GetTrader(isClient,traderID); }

    /**
     * Gets the {@link TraderData} with the given trader id.
     * @param context The context of whether we wish to access the client-side data, or the server-side data.
     * @see #GetTrader(boolean, long)
     */
    @Nullable
    public final TraderData GetTrader(@Nonnull IClientTracker context, long traderID) { return this.GetTrader(context.isClient(),traderID); }
    /**
     * Gets the {@link TraderData} with the given trader id.
     * @param isClient Whether we wish to access the client-side data, or the server-side data.
     * @see #GetTrader(IClientTracker, long)
     */
    @Nullable
    public abstract TraderData GetTrader(boolean isClient, long traderID);

    /**
     * @deprecated Use {@link #GetAllTraders(IClientTracker)} instead.
     * @see #GetAllTraders(boolean)
     * @see #API
     */
    @Deprecated
    @Nonnull
    public static List<TraderData> getAllTraders(@Nonnull IClientTracker clientTracker) { return getAllTraders(clientTracker.isClient()); }
    /**
     * @deprecated Use {@link #GetAllTraders(boolean)} instead.
     * @see #GetAllTraders(IClientTracker)
     * @see #API
     */
    @Deprecated
    @Nonnull
    public static List<TraderData> getAllTraders(boolean isClient) { return TraderSaveData.GetAllTraders(isClient); }

    /**
     * Gets a list of all {@link TraderData} that exist
     * @param context The context of whether we wish to access the client-side data, or the server-side data.
     * @see #GetAllTraders(boolean)
     */
    @Nonnull
    public final List<TraderData> GetAllTraders(@Nonnull IClientTracker context) { return this.GetAllTraders(context.isClient()); }
    /**
     * Gets a list of all {@link TraderData} that exist
     * @param isClient Whether we wish to access the client-side data, or the server-side data.
     * @see #GetAllTraders(IClientTracker)
     */
    @Nonnull
    public abstract List<TraderData> GetAllTraders(boolean isClient);

    /**
     * @deprecated Use {@link #GetAllNetworkTraders(IClientTracker)} instead.
     * @see #GetAllNetworkTraders(boolean)
     * @see #API
     */
    @Deprecated
    @Nonnull
    public static List<TraderData> getAllTerminalTraders(@Nonnull IClientTracker clientTracker) { return API.GetAllNetworkTraders(clientTracker); }
    /**
     * @deprecated Use {@link #GetAllNetworkTraders(boolean)} instead.
     * @see #GetAllNetworkTraders(IClientTracker)
     * @see #API
     */
    @Deprecated
    @Nonnull
    public static List<TraderData> getAllTerminalTraders(boolean isClient) { return getAllTraders(isClient).stream().filter(TraderData::showOnTerminal).collect(Collectors.toList()); }

    /**
     * Gets a list of all {@link TraderData} that exist and are visible from a Network Terminal
     * @param context The context of whether we wish to access the client-side data, or the server-side data
     * @see #GetAllNetworkTraders(IClientTracker)
     */
    public final List<TraderData> GetAllNetworkTraders(@Nonnull IClientTracker context) { return this.GetAllNetworkTraders(context.isClient()); }
    /**
     * Gets a list of all {@link TraderData} that exist and are visible from a Network Terminal
     * @param isClient Whether we wish to access the client-side data, or the server-side data
     * @see #GetAllNetworkTraders(IClientTracker)
     */
    public abstract List<TraderData> GetAllNetworkTraders(boolean isClient);

    /**
     * @deprecated Use {@link #CreateTrader(TraderData)} instead.
     * @see #CreateTrader(TraderData,Player)
     * @see #API
     */
    @Deprecated
    public static long addTrader(@Nonnull TraderData newTrader) { return API.CreateTrader(newTrader); }
    /**
     * @deprecated Use {@link #CreateTrader(TraderData,Player)} instead.
     * @see #CreateTrader(TraderData)
     * @see #API
     */
    @Deprecated
    public static long addTrader(@Nonnull TraderData newTrader, @Nullable Player player) { return API.CreateTrader(newTrader,player); }

    /**
     * Adds the new trader to the Trader Save Data so that it can be accessed, saved, and loaded by the system<br>
     * This variant will build the trader with no owner by default<br>
     * Use {@link #CreateTrader(TraderData, Player)} to define the player who placed to trader so that they will be flagged as the owner
     * @return The Trader ID of the added trader
     * @see #CreateTrader(TraderData, Player)
     */
    public final long CreateTrader(@Nonnull TraderData newTrader) { return this.CreateTrader(newTrader,null); }
    /**
     * Adds the new trader to the Trader Save Data so that it can be accessed, saved, and loaded by the system
     * @param player The player who placed/built the trader. If not null, this will automatically set this player as the traders owner
     * @return The Trader ID of the added trader
     * @see #CreateTrader(TraderData) 
     */
    public abstract long CreateTrader(@Nonnull TraderData newTrader, @Nullable Player player);

    /**
     * @deprecated Use {@link #DeleteTrader(TraderData)} instead.
     * @see #DeleteTrader(long)
     * @see #API
     */
    @Deprecated
    public static void deleteTrader(@Nonnull TraderData trader) { deleteTrader(trader.getID()); }
    /**
     * @deprecated Use {@link #DeleteTrader(long)} instead.
     * @see #DeleteTrader(TraderData)
     * @see #API
     */
    @Deprecated
    public static void deleteTrader(long traderID) { TraderSaveData.DeleteTrader(traderID); }

    /**
     * Deletes the given trader from the system, making it inaccessible and removed from the save data
     * @param trader The Trader to delete
     * @see #DeleteTrader(long)
     */
    public final void DeleteTrader(@Nonnull TraderData trader) { this.DeleteTrader(trader.getID()); }
    /**
     * Deletes the given trader from the system, making it inaccessible and removed from the save data
     * @param traderID The ID of the Trader to delete
     * @see #DeleteTrader(TraderData)
     */
    public abstract void DeleteTrader(long traderID);

}
