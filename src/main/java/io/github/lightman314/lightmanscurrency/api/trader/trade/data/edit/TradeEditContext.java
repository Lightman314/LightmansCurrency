package io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit;

import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;

public record TradeEditContext(boolean hasShiftDown,boolean hasCtrlDown,boolean hasAltDown,ScreenPosition mousePos,ITradeInteractionHandler handler) {

    public FancyPacketMap encode()
    {
        return FancyPacketMap.newMutable()
                .setBoolean("heldShift",this.hasShiftDown)
                .setBoolean("heldCtrl",this.hasCtrlDown)
                .setBoolean("heldAlt",this.hasAltDown)
                .setInt("mouseX",this.mousePos.x)
                .setInt("mouseY",this.mousePos.y);
    }

    public static TradeEditContext decode(FancyPacketMap data,ITradeInteractionHandler handler) {
        return new TradeEditContext(data.getBoolean("heldShift"),data.getBoolean("heldCtrl"),data.getBoolean("heldAlt"),ScreenPosition.of(data.getInt("mouseX"),data.getInt("mouseY")),handler);
    }

}