package io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit;

import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;

public enum TradeSlotType {
    INPUT,OUTPUT,OTHER;
    public boolean isInput() { return this == INPUT; }
    public boolean isOutput() { return this == OUTPUT; }
    public boolean isOther() { return this == OTHER; }
    public boolean isPriceSlot(TradeDirection direction) { return (this.isInput() && direction.isSale()) || (this.isOutput() && direction.isPurchase()); }
}
