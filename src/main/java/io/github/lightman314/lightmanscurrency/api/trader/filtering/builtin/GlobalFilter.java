package io.github.lightman314.lightmanscurrency.api.trader.filtering.builtin;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.FilterHelper;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.IExtraTradeSearchFilter;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.IExtraTraderSearchFilter;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.PendingSearch;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.interfaces.ISearchFilterPrice;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IDisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.filtering.interfaces.ISearchFilterNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import java.util.function.Predicate;

public final class GlobalFilter implements IExtraTraderSearchFilter, IExtraTradeSearchFilter {

    private GlobalFilter() {}

    public static final GlobalFilter INSTANCE = new GlobalFilter();

    @Override
    public void applySearchFilters(TraderData trader,PendingSearch search) {
        search.processFilter(FilterHelper.TYPE,this.checkType(trader));
        search.processFilter(FilterHelper.OWNER,this.checkOwner(trader));
        search.processFilter(FilterHelper.NAME,this.checkName(trader));
        FilterHelper.longRange(search,FilterHelper.ID,trader.getID());
        FilterHelper.intRange(search,FilterHelper.TRADE_COUNT,TradingNode.getCustomerReadyTradeCount(trader));
        FilterHelper.intRange(search,FilterHelper.STOCK_COUNT,TradingNode.getTradesInStock(trader,search.getTransaction()));
        FilterHelper.boolCheck(search,FilterHelper.CUSTOMER_READY,this.isCustomerReady(trader));
    }

    @Override
    public void applySearchFilters(TradeData trade,PendingSearch search) {
        //Apply trade type filter
        search.processFilter(FilterHelper.TRADE_TYPE,this.checkTradeType(trade));
        FilterHelper.longRange(search,FilterHelper.TRADE_STOCK,this.getTradeStock(trade,search));
        //Apply trade price filter
        if(trade.isCustomerReady() && trade.getInternalPrice() instanceof ISearchFilterPrice price)
            price.applySearchFilters(trade,search);
    }

    private Predicate<String> checkType(TraderData trader) {
        String typeString = trader.getType().getKey().toString();
        return type -> {
            //If namespace is provided, require that the traders id starts with the given type string
            if(type.contains(":"))
                return typeString.startsWith(type);
            else //Otherwise, simple check if the given string is part of the traders type
                return typeString.contains(type);
        };
    }

    private Predicate<String> checkTradeType(TradeData trade) {
        String typeString = trade.getType().getKey().toString();
        return type -> {
            //If namespace is provided, require that the traders id starts with the given type string
            if(type.contains(":"))
                return typeString.startsWith(type);
            else //Otherwise, simple check if the given string is part of the traders type
                return typeString.contains(type);
        };
    }

    private Predicate<String> checkOwner(TraderData trader) {
        return owner -> trader.getOwner().getName().getString().toLowerCase().contains(owner.toLowerCase());
    }

    private Predicate<String> checkName(TraderData trader) {
        return name -> IDisplayNode.getTraderName(trader).getString().toLowerCase().contains(name.toLowerCase());
    }

    private long getTradeStock(TradeData trade,PendingSearch search) {
        TradingNode<?> parent = trade.getHolder();
        if(parent != null) {
            TraderData trader = parent.getTrader();
            return trade.predictStock(trader,search.getTransaction());
        }
        return 0;
    }

    private boolean isCustomerReady(TraderData trader) {
        return trader.getNodes(ISearchFilterNode.class).stream().anyMatch(ISearchFilterNode::isReadyForCustomers);
    }

}