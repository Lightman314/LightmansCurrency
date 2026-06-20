package io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;

public record TradeEditContext(boolean hasShiftDown,boolean hasCtrlDown,boolean hasAltDown) {

    public FancyPacketMap encode()
    {
        return FancyPacketMap.newMutable()
                .setBoolean("heldShift",this.hasShiftDown)
                .setBoolean("heldCtrl",this.hasCtrlDown)
                .setBoolean("heldAlt",this.hasAltDown);
    }

    public static TradeEditContext decode(FancyPacketMap data) {
        return new TradeEditContext(data.getBoolean("heldShift"),data.getBoolean("heldCtrl"),data.getBoolean("heldAlt"));
    }

    public interface Builder
    {
        TradeEditContext create(boolean heldShift,boolean heldCrtl,boolean heldAlt);
    }

}