package io.github.lightman314.lightmanscurrency.menus.traderstorage.auction;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction.AuctionTradeCancelClientTab;
import io.github.lightman314.lightmanscurrency.common.universal_traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.AuctionTradeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AuctionTradeCancelTab extends TraderStorageTab {
	
	public AuctionTradeCancelTab(TraderStorageMenu menu) { super(menu); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new AuctionTradeCancelClientTab(screen, this); }
	
	@Override
	public boolean canOpen(Player player) { return this.menu.getTrader() instanceof AuctionHouseTrader; }
	
	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	public AuctionTradeData getTrade() { 
		if(this.menu.getTrader() instanceof AuctionHouseTrader)
		{
			AuctionHouseTrader trader = (AuctionHouseTrader)this.menu.getTrader();
			if(this.tradeIndex >= trader.getTradeCount() || this.tradeIndex < 0)
			{
				this.menu.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
				this.menu.sendMessage(this.menu.createTabChangeMessage(TraderStorageTab.TAB_TRADE_BASIC, null));
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
		ITrader t = this.menu.getTrader();
		if(t instanceof AuctionHouseTrader)
		{
			AuctionHouseTrader trader = (AuctionHouseTrader)t;
			AuctionTradeData trade = trader.getTrade(this.tradeIndex);
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putBoolean("CancelAuction", giveToPlayer);
				this.menu.sendMessage(message);
				//Don't run the cancel interaction while on the client
				return;
			}
			if(trade.isOwner(this.menu.player))
			{
				trade.CancelTrade(trader, giveToPlayer, this.menu.player);
				trader.markTradesDirty();
			}
		}
	}
	
	@Override
	public void receiveMessage(CompoundTag message) {
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
