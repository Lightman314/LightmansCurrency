package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.auction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction.AuctionCreateClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class AuctionCreateTab extends TraderStorageTab {
	
	public AuctionCreateTab(@Nonnull ITraderStorageMenu menu) { super(menu); }
	
	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(@Nonnull Object screen) { return new AuctionCreateClientTab(screen, this); }
	
	@Override
	public boolean canOpen(Player player) { return true; }
	
	List<EasySlot> slots = new ArrayList<>();
	public List<EasySlot> getSlots() { return this.slots; }
	SimpleContainer auctionItems = new SimpleContainer(2);
	public SimpleContainer getAuctionItems() { return this.auctionItems; }
	
	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
		
		for(int i = 0; i < this.auctionItems.getContainerSize(); ++i)
		{
			EasySlot newSlot = new EasySlot(this.auctionItems, i, TraderMenu.SLOT_OFFSET + 8 + i * 18, 122);
			addSlot.apply(newSlot);
			this.slots.add(newSlot);
		}
		EasySlot.SetActive(this.slots, false);
		
	}
	
	@Override
	public void onTabOpen() {
		EasySlot.SetActive(this.slots);
		for(EasySlot slot : this.slots)
			slot.locked = false;
	}
	
	@Override
	public void onTabClose() {
		EasySlot.SetInactive(this.slots);
		this.menu.clearContainer(this.auctionItems);
	}
	
	@Override
	public void onMenuClose() { this.menu.clearContainer(this.auctionItems); }
	
	public void createAuction(AuctionTradeData trade) {
		TraderData t = this.menu.getTrader();
		if(t instanceof AuctionHouseTrader trader)
		{
			if(this.menu.isClient())
			{
				this.menu.SendMessage(this.builder().setCompound("CreateAuction", trade.getAsNBT(this.registryAccess())));
				return;
			}
			//Set the trade's auction items based on the items currently in the auction item slots
			trade.setAuctionItems(this.auctionItems);
			if(!trade.isValid())
			{
				//Send failure message to the client.
				this.menu.SendMessage(this.builder().setBoolean("AuctionCreated", false));
				//LightmansCurrency.LogInfo("Failed to create the auction as the auction is not valid.");
				return;
			}
			trader.addTrade(trade, false);
			//Delete the contents of the auctionItems
			this.auctionItems.clearContent();
			//Send response message to the client
			this.menu.SendMessage(this.builder().setBoolean("AuctionCreated", true));
			for(EasySlot slot : this.slots) slot.locked = true;
			//LightmansCurrency.LogInfo("Successfully created the auction!");
		}
	}

	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("CreateAuction"))
		{
			//LightmansCurrency.LogInfo("Received Auction from the client.\n" + message.getCompound("CreateAuction").getAsString());
			this.createAuction(new AuctionTradeData(message.getNBT("CreateAuction"),message.lookup));
		}
	}

}
