package io.github.lightman314.lightmanscurrency.api.traders;

/**
 * Interface to be applied to traders whose offer count can be modified by a {@link io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades#TRADE_OFFERS TradeOfferUpgrade}
 */
public interface IFlexibleOfferTrader {

    void refactorTrades();

}
