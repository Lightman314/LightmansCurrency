package io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit;

import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;

public interface ITradeInteractionHandler {

    void onTradeSlotClick(TradeData trade,TradeSlotType type,int slotIndex,int mouseButton,TradeEditContext context);
    void onTradeSlotScroll(TradeData trade,TradeSlotType type,int slotIndex,float deltaY,TradeEditContext context);

}