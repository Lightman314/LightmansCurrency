package io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;

public interface ITradeInteractionHandler {

    ITradeInteractionHandler NULL = new Null();

    void onTradeSlotClick(TradeData trade,TradeSlot slot,int mouseButton,TradeEditContext context);
    void onTradeSlotScroll(TradeData trade,TradeSlot slot,float deltaY,TradeEditContext context);

    boolean allowsScrollInteractions();
    default boolean isSimpleEdit() { return !this.isAdvancedEdit(); }
    boolean isAdvancedEdit();
    default boolean isSelected(TradeSlot slot) { return false; }
    default void changeSelection(TradeSlot slot) {}
    default void openAdvancedEdit(TradeData trade,TradeSlot slot) {}
    void sendPriceEditPacket(TradeData trade,FancyPacketMap packet);

    final class Null implements ITradeInteractionHandler {
        private Null() {}

        @Override
        public void onTradeSlotClick(TradeData trade, TradeSlot slot, int mouseButton, TradeEditContext context) {}
        @Override
        public void onTradeSlotScroll(TradeData trade, TradeSlot slot, float deltaY, TradeEditContext context) { }
        @Override
        public boolean allowsScrollInteractions() { return false; }
        @Override
        public boolean isSimpleEdit() { return false; }
        @Override
        public boolean isAdvancedEdit() { return false; }
        @Override
        public void sendPriceEditPacket(TradeData trade,FancyPacketMap packet) {}
    }

}