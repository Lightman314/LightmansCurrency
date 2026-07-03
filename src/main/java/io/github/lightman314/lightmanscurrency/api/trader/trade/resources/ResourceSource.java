package io.github.lightman314.lightmanscurrency.api.trader.trade.resources;

import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;

public enum ResourceSource {
    TRADER,CUSTOMER;

    /**
     * Calculates the resource source to use extracting the <b>product</b> to later be given to its target<br>
     *
     */
    public static ResourceSource fromResource(TradeDirection direction) {
        if(direction.isOther())
            throw new UnsupportedOperationException("Cannot get the directional resource side from an 'Other' trade direction!");
        return direction.isSale() ? ResourceSource.TRADER : ResourceSource.CUSTOMER;
    }
    public static ResourceSource toResource(TradeDirection direction) {
        if(direction.isOther())
            throw new UnsupportedOperationException("Cannot get the directional resource side from an 'Other' trade direction!");
        return direction.isSale() ? ResourceSource.CUSTOMER : ResourceSource.TRADER;
    }

}