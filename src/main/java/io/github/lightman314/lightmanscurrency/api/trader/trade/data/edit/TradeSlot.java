package io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit;

import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;

import java.util.function.Consumer;

public record TradeSlot(TradeSlotType type,int slot) {

    public static final TradeSlot NONE = new TradeSlot(TradeSlotType.EARLY, -1);

    public boolean isEarly() { return this.type == TradeSlotType.EARLY; }
    public boolean isInput() { return this.type == TradeSlotType.INPUT; }
    public boolean isArrow() { return this.type == TradeSlotType.ARROW; }
    public boolean isOutput() { return this.type == TradeSlotType.OUTPUT; }
    public boolean isLate() { return this.type == TradeSlotType.LATE; }
    public boolean isInputOrOutput() { return this.isInput() || this.isOutput(); }
    public boolean isPriceSlot(TradeDirection direction) { return (this.isInput() && direction.isSale()) || (this.isOutput() && direction.isPurchase()); }

    public boolean isSameType(TradeSlot other) { return this.type == other.type; }

    public static TradeSlot defaultPriceSlot(TradeDirection direction) {
        if(direction.isSale())
            return new TradeSlot(TradeSlotType.INPUT,0);
        if(direction.isPurchase())
            return new TradeSlot(TradeSlotType.OUTPUT,0);
        return NONE;
    }

    public TradeSlot flipped() {
        TradeSlotType newType = EnumHelper.enumFromOrdinal(TradeSlotType.values().length - this.type.ordinal() - 1,TradeSlotType.values(),null);
        if(newType == null || newType == this.type)
            return this;
        return new TradeSlot(newType,this.slot);
    }

    public Consumer<FancyPacketMap.Mutable> encode() {
        return packet -> packet.setEnum("type",this.type).setInt("slot",this.slot);
    }

    public static TradeSlot decode(FancyPacketMap packet) {
        return new TradeSlot(packet.getEnum("type",TradeSlotType.class),packet.getInt("slot"));
    }

}