package io.github.lightman314.lightmanscurrency.api.trader;

import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.IExtraTradeSearchFilter;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.IExtraTraderSearchFilter;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.interfaces.ISearchFilterNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.interfaces.ISearchFilterTrade;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.List;

public interface TraderAPI {


    /**
     * Registers an {@link IExtraTraderSearchFilter} filter that will be applied in addition to the trader's built-in {@link ISearchFilterNode} filters.
     */
    void registerExtraTraderSearchFilter(IExtraTraderSearchFilter filter);
    /**
     * Registers an {@link IExtraTradeSearchFilter} filter that will be applied in addition to the trader's built-in {@link ISearchFilterTrade} filters.
     */
    void registerExtraTradeSearchFilter(IExtraTradeSearchFilter filter);

    default <T extends IExtraTraderSearchFilter & IExtraTradeSearchFilter> void registerExtraSearchFilter(T filter) {
        this.registerExtraTraderSearchFilter(filter);
        this.registerExtraTradeSearchFilter(filter);
    }

    /**
     * Whether the given trader matches the given search text, and should be listed in the search results
     */
    boolean filterTrader(TraderData trader,String searchText,@Nullable TransactionContext transaction);

    /**
     * Filters the given list via {@link #filterTrader(TraderData,String,TransactionContext)} and assembles a new list of the traders that passed
     */
    List<TraderData> filterTraders(List<TraderData> traders,String searchText,@Nullable TransactionContext transaction);

    /**
     * Whether the given trade matches the given search text, and should be listed in the search results
     */
    boolean filterTrade(TradeData trade,String searchText,@Nullable TransactionContext transaction);
    /**
     * Filters the given list via {@link #filterTrade(TradeData,String,TransactionContext)} and assembles a neww list of the  trades that passed
     */
    List<TradeData> filterTrades(List<TradeData> trades,String searchText,@Nullable TransactionContext transaction);

    /**
     * Applies the trades search filters to the current search.
     */
    void applyTradeFilters(TradeData trade,PendingSearch search);
    /**
     * Apples the trades search filters to the current search
     */
    default void applyTradeFilters(List<? extends TradeData> trades,PendingSearch search) {
        for(TradeData trade : trades)
            this.applyTradeFilters(trade,search);
    }

    /**
     * Gets the {@link TraderData} with the given trader id.
     * @param isClient The context of whether we wish to access the client-side data, or the server-side data.
     */
    default TraderData getTrader(boolean isClient,long traderID) { return this.getTrader(ISidedContext.known(isClient),traderID); }
    /**
     * Gets the {@link TraderData} with the given trader id.
     * @param context The context of whether we wish to access the client-side data, or the server-side data.
     */
    @Nullable
    TraderData getTrader(ISidedContext context,long traderID);

    /**
     * Gets a list of all {@link TraderData} that exist
     * @param isClient Whether we wish to access the client-side data, or the server-side data.
     */
    default List<TraderData> getAllTraders(boolean isClient) { return this.getAllTraders(ISidedContext.known(isClient)); }
    /**
     * Gets a list of all {@link TraderData} that exist
     * @param context The context of whether we wish to access the client-side data, or the server-side data.
     */
    List<TraderData> getAllTraders(ISidedContext context);

    /**
     * Gets a list of all {@link TraderData} that exist and are visible from a Network Terminal
     * @param isClient Whether we wish to access the client-side data, or the server-side data
     */
    default List<TraderData> getAllNetworkTraders(boolean isClient) { return this.getAllNetworkTraders(ISidedContext.known(isClient)); }
    /**
     * Gets a list of all {@link TraderData} that exist and are visible from a Network Terminal
     * @param context The context of whether we wish to access the client-side data, or the server-side data
     */
    List<TraderData> getAllNetworkTraders(ISidedContext context);

    /**
     * Adds the new trader to the Trader Save Data so that it can be accessed, saved, and loaded by the system
     * @return The Trader ID of the added trader
     */
    long initializeTrader(TraderData newTrader);

    /**
     * Deletes the given trader from the system, making it inaccessible and removed from the save data
     * @param trader The Trader to delete
     * @see #deleteTrader(long)
     */
    default void deleteTrader(TraderData trader) { this.deleteTrader(trader.getID()); }
    /**
     * Deletes the given trader from the system, making it inaccessible and removed from the save data
     * @param traderID The ID of the Trader to delete
     * @see #deleteTrader(TraderData)
     */
    void deleteTrader(long traderID);

}