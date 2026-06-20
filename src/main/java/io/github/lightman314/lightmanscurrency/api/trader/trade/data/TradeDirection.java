package io.github.lightman314.lightmanscurrency.api.trader.trade.data;

import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;

/**
 * Enum declaring the intended interaction direction of the trade<br>
 * {@link #SALE} indicates that the trade is selling the product in exchange for the given {@link TradePrice}<br>
 * {@link #PURCHASE} indicates that the trade is buying the product in exchange for the given {@link TradePrice}<br>
 * {@link #OTHER} indicates that the trade is doing some form of custom interaction that either doesn't involve the {@link TradePrice} or is doing some form of unknown or abnormal trade interaction
 */
public enum TradeDirection {
    SALE,PURCHASE,OTHER;
    public boolean isSale() { return this == SALE; }
    public boolean isPurchase() { return this == PURCHASE; }
    public boolean isOther() { return this == OTHER; }
}