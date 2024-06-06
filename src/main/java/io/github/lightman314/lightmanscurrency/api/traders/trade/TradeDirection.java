package io.github.lightman314.lightmanscurrency.api.traders.trade;

import io.github.lightman314.lightmanscurrency.LCText;
import net.minecraft.network.chat.MutableComponent;

public enum TradeDirection { SALE(0), PURCHASE(1), BARTER(2), OTHER(-1);
    public final int index;
    TradeDirection(int index) { this.index = index; }
    public static TradeDirection fromIndex(int index) {
        for(TradeDirection d : TradeDirection.values())
        {
            if(d.index == index)
                return d;
        }
        return TradeDirection.SALE;
    }
    public final MutableComponent getName() { return LCText.GUI_TRADE_DIRECTION.get(this).get(); }
    public final MutableComponent getActionPhrase() { return LCText.GUI_TRADE_DIRECTION_ACTION.get(this).get(); }
}
