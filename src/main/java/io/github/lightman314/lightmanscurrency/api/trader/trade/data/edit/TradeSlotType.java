package io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit;

import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;

public enum TradeSlotType {
    EARLY,INPUT,ARROW,OUTPUT,LATE;
    public boolean isEarly() { return this == EARLY; }
    public boolean isInput() { return this == INPUT; }
    public boolean isArrow() { return this == ARROW; }
    public boolean isOutput() { return this == OUTPUT; }
    public boolean isLate() { return this == LATE; }

    public boolean isLeftHalf() { return this.isEarly() || this.isInput(); }
    public boolean isRightHalf() { return this.isOutput() || this.isLate(); }

    public boolean isPriceSlot(TradeDirection direction) { return (this.isInput() && direction.isSale()) || (this.isOutput() && direction.isPurchase()); }
}
