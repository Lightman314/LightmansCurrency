package io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;

import java.util.function.Consumer;

public record TradeSlot(TradeSlotType type,int slot) {

    public static final TradeSlot NONE = new TradeSlot(TradeSlotType.OTHER,-1);

    public boolean isInput() { return this.type == TradeSlotType.INPUT; }
    public boolean isArrow() { return this.type == TradeSlotType.ARROW; }
    public boolean isOutput() { return this.type == TradeSlotType.OUTPUT; }
    public boolean isOther() { return this.type == TradeSlotType.OTHER; }
    public boolean isPriceSlot(TradeDirection direction) { return (this.isInput() && direction.isSale()) || (this.isOutput() && direction.isPurchase()); }

    public Consumer<FancyPacketMap.Mutable> encode() {
        return packet -> packet.setEnum("type",this.type).setInt("slot",this.slot);
    }

    public static TradeSlot decode(FancyPacketMap packet) {
        return new TradeSlot(packet.getEnum("type",TradeSlotType.class),packet.getInt("slot"));
    }

}