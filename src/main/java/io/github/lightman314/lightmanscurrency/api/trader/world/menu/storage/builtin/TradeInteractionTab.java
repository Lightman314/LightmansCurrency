package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.builtin;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeSet;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import net.minecraft.world.entity.player.Player;
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

    protected final boolean canInteract(TradeData trade) { return this.editableTrades().contains(trade) && this.getPermission(BuiltInPermissions.EDIT_TRADES); }

    @Override
    public final void onTradeSlotClick(TradeData trade,TradeSlot slot,int mouseButton,TradeEditContext context)
    {
        if(!this.canInteract(trade))
            return;
        ItemStack heldItem = this.getMenu().getCarried();
        if(this.processTradeClick(this.getPlayer(),trade,slot,mouseButton,heldItem,context) && this.isClient())
        {
            FancyPacketMap.Mutable packet = FancyPacketMap.newMutable();
            this.writeTargetTrade(packet,trade);
            this.sendToServer(FancyPacketMap.newMutable()
                    .setMap("tradeClick",packet
                            .action(slot.encode())
                            .setInt("mouse",mouseButton)
                            .setMap("context",context.encode())));
        }
    }
    protected abstract boolean processTradeClick(Player player,TradeData trade,TradeSlot slot,int mouseButton,ItemStack heldItem,TradeEditContext context);

    @Override
    public void onTradeSlotScroll(TradeData trade,TradeSlot slot, float deltaY, TradeEditContext context) {
        if(!this.canInteract(trade))
            return;
        ItemStack heldItem = this.getMenu().getCarried();
        if(this.processTradeScroll(this.getPlayer(),trade,slot,deltaY,heldItem,context))
        {
            FancyPacketMap.Mutable packet = FancyPacketMap.newMutable();
            this.writeTargetTrade(packet,trade);
            this.sendToServer(FancyPacketMap.newMutable()
                    .setMap("tradeScroll",packet
                            .setEnum("type",slot.type())
                            .setInt("slot",slot.slot())
                            .setFloat("scroll",deltaY)
                            .setMap("context",context.encode())));
        }
    }
    protected abstract boolean processTradeScroll(Player player, TradeData trade, TradeSlot slot, float deltaY, ItemStack heldItem, TradeEditContext context);

    @Override
    public void sendPriceEditPacket(TradeData trade,FancyPacketMap packet) {
        //Make it mutable and write the target trade
        FancyPacketMap.Mutable edit = packet.mutable();
        this.writeTargetTrade(edit,trade);
        //Send the packet
        this.sendToServer(FancyPacketMap.newMutable()
                .setMap("priceEdit",edit));
    }

    protected void writeTargetTrade(FancyPacketMap.Mutable edit, TradeData trade)
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
                        TradeSlot.decode(edit),
                        edit.getInt("mouse"),
                        TradeEditContext.decode(edit.getMap("context"),this));
            }
        }
        if(packet.contains("tradeScroll"))
        {
            FancyPacketMap edit = packet.getMap("tradeScroll");
            TradeData trade = this.getTargetTrade(edit);
            if(trade != null)
            {
                this.onTradeSlotScroll(trade,
                        TradeSlot.decode(edit),
                        edit.getFloat("scroll"),
                        TradeEditContext.decode(edit.getMap("context"),this));
            }
        }
        if(packet.contains("priceEdit"))
        {
            FancyPacketMap edit = packet.getMap("priceEdit");
            TradeData trade = this.getTargetTrade(edit);
            if(trade != null)
                trade.getPrice().handleCustomEditMessage(edit);
        }
    }

}
