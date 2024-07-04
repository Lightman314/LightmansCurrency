package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.auction;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction.AuctionTradeCancelClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class AuctionTradeCancelTab extends TraderStorageTab {
	
	public AuctionTradeCancelTab(@Nonnull ITraderStorageMenu menu) { super(menu); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(Object screen) { return new AuctionTradeCancelClientTab(screen, this); }
	
	@Override
	public boolean canOpen(Player player) { return true; }
	
	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	public AuctionTradeData getTrade() { 
		if(this.menu.getTrader() instanceof AuctionHouseTrader trader)
		{
			if(this.tradeIndex >= trader.getTradeCount() || this.tradeIndex < 0)
			{
				this.menu.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
				this.menu.SendMessage(this.menu.createTabChangeMessage(TraderStorageTab.TAB_TRADE_BASIC, null));
				return null;
			}
			return ((AuctionHouseTrader)this.menu.getTrader()).getTrade(this.tradeIndex);
		}
		return null;
	}
	
	@Override
	public void onTabOpen() { }

	@Override
	public void onTabClose() { }
	
	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }
	
	public void setTradeIndex(int tradeIndex) { this.tradeIndex = tradeIndex; }
	
	public void cancelAuction(boolean giveToPlayer) {
		TraderData t = this.menu.getTrader();
		if(t instanceof AuctionHouseTrader trader)
		{
			AuctionTradeData trade = trader.getTrade(this.tradeIndex);
			if(this.menu.isClient())
			{
				this.menu.SendMessage(this.builder().setBoolean("CancelAuction", giveToPlayer));
				//Don't run the cancel interaction while on the client
				return;
			}
			if(trade.isOwner(this.menu.getPlayer()))
			{
				trade.CancelTrade(trader, giveToPlayer, this.menu.getPlayer());
				trader.markTradesDirty();
				trader.markStorageDirty();
				this.menu.SendMessage(this.builder().setBoolean("CancelSuccess", true));
			}
		}
	}

	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("TradeIndex"))
		{
			this.tradeIndex = message.getInt("TradeIndex");
		}
		if(message.contains("CancelAuction"))
		{
			this.cancelAuction(message.getBoolean("CancelAuction"));
		}
	}

}
