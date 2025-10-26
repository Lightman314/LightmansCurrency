package io.github.lightman314.lightmanscurrency.api.traders;

import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITradeSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.ITraderSearchFilter;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.SortTypeKey;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.impl.TraderAPIImpl;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class TraderAPI {

    private static TraderAPI instance;
    public static TraderAPI getApi() {
        if(instance == null)
            instance = new TraderAPIImpl();
        return instance;
    }

    protected TraderAPI() { if(instance != null)  throw new IllegalCallerException("Cannot create a new TraderAPI instance as one is already present!"); }

    /**
     * Registers the given {@link TraderType}, allowing {@link TraderData} of that type to be saved &amp; loaded from the Trader Save Data.
     */
    public abstract void RegisterTrader(TraderType<?> type);

    /**
     * Accesses the {@link TraderType} registry and gets the {@link TraderType} for the given id.
     * @see #RegisterTrader(TraderType)
     */
    @Nullable
    public abstract TraderType<?> GetTraderType(ResourceLocation type);

    /**
     * Registers the given {@link TradeRuleType} allowing {@link io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule Trade Rule}'s of that type to be added, saved, and loaded from any relevant trades/traders.
     */
    public abstract void RegisterTradeRule(TradeRuleType<?> type);

    /**
     * Accesses the {@link TradeRuleType} registry and gets the {@link TradeRuleType} for the given id.
     * @see #RegisterTradeRule(TradeRuleType)
     */
    @Nullable
    public abstract TradeRuleType<?> GetTradeRuleType(ResourceLocation type);

    /**
     * Returns an Immutable list of all registered {@link TradeRuleType}'s<br>
     * @see #RegisterTradeRule(TradeRuleType)
     */
    public abstract List<TradeRuleType<?>> GetAllTradeRuleTypes();

    /**
     * Registers the given {@link ITraderSearchFilter}, allowing the ability to search traders via {@link #FilterTrader(TraderData, String)} &amp; {@link #FilterTraders(List, String)}
     */
    public abstract void RegisterTraderSearchFilter(ITraderSearchFilter filter);

    /**
     * Whether the given trader matches the given search text, and should be listed in the search results
     * @see #RegisterTraderSearchFilter(ITraderSearchFilter)
     */
    public abstract boolean FilterTrader(TraderData data, String searchText);

    /**
     * Filters the given list via {@link #FilterTrader(TraderData, String)} and returns the list of traders that have passed
     */
    
    public abstract List<TraderData> FilterTraders(List<TraderData> data, String searchText);

    /**
     * Registers the given {@link ITradeSearchFilter}, allowing the ability to search traders via {@link #FilterTrade(TradeData,String,RegistryAccess)} &amp; {@link #FilterTrades(List,String,RegistryAccess)}
     */
    public abstract void RegisterTradeSearchFilter(ITradeSearchFilter filter);
    /**
     * Whether the given trade matches the given search text, and should be listed in the search results
     * @see #RegisterTradeSearchFilter(ITradeSearchFilter)
     */
    public abstract boolean FilterTrade(TradeData trade, String searchText, RegistryAccess registryAccess);
    /**
     * Filters the given list via {@link #FilterTrade(TradeData, String,RegistryAccess)} and returns the list of trades that have passed
     */
    
    public abstract List<TradeData> FilterTrades(List<TradeData> trades, String searchText, RegistryAccess registryAccess);

    /**
     *  Registers the given {@link ITraderSearchFilter} &amp; {@link ITradeSearchFilter} so that the trader &amp; its trades can be filtered.
     * @see #RegisterTraderSearchFilter(ITraderSearchFilter)
     * @see #RegisterTradeSearchFilter(ITradeSearchFilter)
     */
    public abstract <T extends ITraderSearchFilter & ITradeSearchFilter> void RegisterSearchFilter(T filter);

    /**
     * Registers the given {@link TerminalSortType} as a valid sort type option
     */
    public abstract void RegisterSortType(TerminalSortType sortType);

    @Nullable
    public abstract TerminalSortType GetSortType(ResourceLocation key);
    @Nullable
    public abstract TerminalSortType GetSortType(SortTypeKey key);

    /**
     * Returns a list of all registered {@link TerminalSortType}'s
     * @see #RegisterSortType(TerminalSortType)
     */
    public abstract List<TerminalSortType> GetAllSortTypes();

    /**
     * Returns a list of keys for all registered {@link TerminalSortType}
     * @see #GetSortType(ResourceLocation)
     * @see #GetAllSortTypes()
     */
    public abstract List<SortTypeKey> GetAllSortTypeKeys();

    /**
     * Gets the {@link TraderData} with the given trader id.
     * @param context The context of whether we wish to access the client-side data, or the server-side data.
     * @see #GetTrader(boolean, long)
     */
    @Nullable
    public final TraderData GetTrader(IClientTracker context, long traderID) { return this.GetTrader(context.isClient(),traderID); }
    /**
     * Gets the {@link TraderData} with the given trader id.
     * @param isClient Whether we wish to access the client-side data, or the server-side data.
     * @see #GetTrader(IClientTracker, long)
     */
    @Nullable
    public abstract TraderData GetTrader(boolean isClient, long traderID);
    
    /**
     * Gets a list of all {@link TraderData} that exist
     * @param context The context of whether we wish to access the client-side data, or the server-side data.
     * @see #GetAllTraders(boolean)
     */
    
    public final List<TraderData> GetAllTraders(IClientTracker context) { return this.GetAllTraders(context.isClient()); }
    /**
     * Gets a list of all {@link TraderData} that exist
     * @param isClient Whether we wish to access the client-side data, or the server-side data.
     * @see #GetAllTraders(IClientTracker)
     */
    
    public abstract List<TraderData> GetAllTraders(boolean isClient);

    /**
     * Gets a list of all {@link TraderData} that exist and are visible from a Network Terminal
     * @param context The context of whether we wish to access the client-side data, or the server-side data
     * @see #GetAllNetworkTraders(IClientTracker)
     */
    public final List<TraderData> GetAllNetworkTraders(IClientTracker context) { return this.GetAllNetworkTraders(context.isClient()); }
    /**
     * Gets a list of all {@link TraderData} that exist and are visible from a Network Terminal
     * @param isClient Whether we wish to access the client-side data, or the server-side data
     * @see #GetAllNetworkTraders(IClientTracker)
     */
    public abstract List<TraderData> GetAllNetworkTraders(boolean isClient);

    /**
     * Adds the new trader to the Trader Save Data so that it can be accessed, saved, and loaded by the system<br>
     * This variant will build the trader with no owner by default<br>
     * Use {@link #CreateTrader(TraderData, Player)} to define the player who placed to trader so that they will be flagged as the owner
     * @return The Trader ID of the added trader
     * @see #CreateTrader(TraderData, Player)
     */
    public final long CreateTrader(TraderData newTrader) { return this.CreateTrader(newTrader,null); }
    /**
     * Adds the new trader to the Trader Save Data so that it can be accessed, saved, and loaded by the system
     * @param player The player who placed/built the trader. If not null, this will automatically set this player as the traders owner
     * @return The Trader ID of the added trader
     * @see #CreateTrader(TraderData) 
     */
    public abstract long CreateTrader(TraderData newTrader, @Nullable Player player);

    /**
     * Deletes the given trader from the system, making it inaccessible and removed from the save data
     * @param trader The Trader to delete
     * @see #DeleteTrader(long)
     */
    public final void DeleteTrader(TraderData trader) { this.DeleteTrader(trader.getID()); }
    /**
     * Deletes the given trader from the system, making it inaccessible and removed from the save data
     * @param traderID The ID of the Trader to delete
     * @see #DeleteTrader(TraderData)
     */
    public abstract void DeleteTrader(long traderID);

}
