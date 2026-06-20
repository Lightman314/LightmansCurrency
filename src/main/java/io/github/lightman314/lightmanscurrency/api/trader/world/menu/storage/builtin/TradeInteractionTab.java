package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeSet;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlotType;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class TradeInteractionTab extends TraderStorageTab implements ITradeInteractionHandler {

    public TradeInteractionTab(TraderStorageMenu menu) { super(menu); }

    protected boolean isSingleTrade() { return false; }

    protected abstract List<TradeSet> editableTradeSets();
    public List<TradeData> editableTrades() {
        List<TradeData> trades = new ArrayList<>();
        for(TradeSet set : this.editableTradeSets())
            trades.addAll(set.getTrades());
        return trades;
    }

    public final void onTradeSlotClick(TradeData trade,TradeSlotType type,int slotIndex,int mouseButton,TradeEditContext context)
    {
        if(!this.editableTrades().contains(trade))
            return;
        ItemStack heldItem = this.getMenu().getCarried();
        if(this.processTradeClick(trade,type,slotIndex,mouseButton,heldItem,context) && this.isClient())
        {
            FancyPacketMap.Mutable packet = FancyPacketMap.newMutable();
            this.writeTargetTrade(packet,trade);
            this.sendToServer(FancyPacketMap.newMutable()
                    .setMap("tradeClick",packet
                            .setEnum("type",type)
                            .setInt("slot",slotIndex)
                            .setInt("mouse",mouseButton)
                            .setMap("context",context.encode())));
        }
    }
    protected abstract boolean processTradeClick(TradeData trade, TradeSlotType type, int slotIndex, int mouseButton, ItemStack heldItem, TradeEditContext context);

    @Override
    public void onTradeSlotScroll(TradeData trade,TradeSlotType type,int slotIndex,float deltaY,TradeEditContext context) {
        ItemStack heldItem = this.getMenu().getCarried();
        if(this.processTradeScroll(trade,type,slotIndex,deltaY,heldItem,context))
        {
            FancyPacketMap.Mutable packet = FancyPacketMap.newMutable();
            this.writeTargetTrade(packet,trade);
            this.sendToServer(FancyPacketMap.newMutable()
                    .setMap("tradeScroll",packet
                            .setEnum("type",type)
                            .setInt("slot",slotIndex)
                            .setFloat("scroll",deltaY)
                            .setMap("context",context.encode())));
        }
    }
    protected abstract boolean processTradeScroll(TradeData trade,TradeSlotType type,int slotIndex,float deltaY,ItemStack heldItem,TradeEditContext context);

    protected void writeTargetTrade(FancyPacketMap.Mutable edit,TradeData trade)
    {
        if(!this.isSingleTrade())
        {
            //Add the "set" and "trade" arguments to the packet
            List<TradeSet> sets = this.editableTradeSets();
            for(int i = 0; i < sets.size(); ++i)
            {
                TradeSet set = sets.get(i);
                if(set.contains(trade))
                {
                    edit.setInt("set",i)
                            .setInt("trade",set.getTrades().indexOf(trade));
                }
            }
        }
    }

    @Nullable
    protected TradeData getTargetTrade(FancyPacketMap edit)
    {
        TradeData trade = null;
        if(this.isSingleTrade())
        {
            List<TradeData> allTrades = this.editableTrades();
            if(!allTrades.isEmpty())
                trade = allTrades.getFirst();
        }
        else
        {
            List<TradeSet> sets = this.editableTradeSets();
            int setIndex = edit.getInt("set",-1);
            if(setIndex >= 0 && setIndex < sets.size())
            {
                TradeSet set = sets.get(setIndex);
                List<TradeData> trades = set.getTrades();
                int tradeIndex = edit.getInt("trade",-1);
                if(tradeIndex >= 0 && tradeIndex < trades.size())
                    trade = trades.get(tradeIndex);
            }
        }
        return trade;
    }

    @Override
    public void handleMessage(FancyPacketMap packet) {
        if(packet.contains("tradeClick"))
        {
            FancyPacketMap edit = packet.getMap("tradeClick");
            TradeData trade = this.getTargetTrade(edit);
            if(trade != null)
            {
                this.onTradeSlotClick(trade,
                        edit.getEnum("type",TradeSlotType.class),
                        edit.getInt("slot"),
                        edit.getInt("mouse"),
                        TradeEditContext.decode(edit.getMap("context")));
            }
        }
        if(packet.contains("tradeScroll"))
        {
            FancyPacketMap edit = packet.getMap("tradeScroll");
            TradeData trade = this.getTargetTrade(edit);
            if(trade != null)
            {
                this.onTradeSlotScroll(trade,
                        edit.getEnum("type", TradeSlotType.class),
                        edit.getInt("slot"),
                        edit.getFloat("scroll"),
                        TradeEditContext.decode(edit.getMap("context")));
            }
        }
    }

}
