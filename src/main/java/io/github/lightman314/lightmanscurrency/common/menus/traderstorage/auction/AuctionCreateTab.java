package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.auction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction.AuctionCreateClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AuctionCreateTab extends TraderStorageTab {
	
	public AuctionCreateTab(TraderStorageMenu menu) { super(menu); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(Object screen) { return new AuctionCreateClientTab(screen, this); }
	
	@Override
	public boolean canOpen(Player player) { return true; }
	
	List<SimpleSlot> slots = new ArrayList<>();
	public List<SimpleSlot> getSlots() { return this.slots; }
	SimpleContainer auctionItems = new SimpleContainer(2);
	public SimpleContainer getAuctionItems() { return this.auctionItems; }
	
	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
		
		for(int i = 0; i < this.auctionItems.getContainerSize(); ++i)
		{
			SimpleSlot newSlot = new SimpleSlot(this.auctionItems, i, TraderMenu.SLOT_OFFSET + 8 + i * 18, 122);
			addSlot.apply(newSlot);
			this.slots.add(newSlot);
		}
		SimpleSlot.SetActive(this.slots, false);
		
	}
	
	@Override
	public void onTabOpen() {
		SimpleSlot.SetActive(this.slots);
		for(SimpleSlot slot : this.slots)
			slot.locked = false;
	}
	
	@Override
	public void onTabClose() {
		SimpleSlot.SetInactive(this.slots);
		this.menu.clearContainer(this.auctionItems);
	}
	
	@Override
	public void onMenuClose() {
		this.menu.clearContainer(this.auctionItems);
	}
	
	public void createAuction(AuctionTradeData trade) {
		if(this.menu.getTrader() instanceof AuctionHouseTrader trader)
		{
			if(this.menu.isClient())
			{
				this.menu.SendMessage(LazyPacketData.simpleTag("CreateAuction", trade.getAsNBT()));
				return;
			}
			//Set the trade's auction items based on the items currently in the auction item slots
			trade.setAuctionItems(this.auctionItems);
			if(!trade.isValid())
			{
				//Send failure message to the client.
				this.menu.SendMessage(LazyPacketData.simpleBoolean("AuctionCreated", false));
				//LightmansCurrency.LogInfo("Failed to create the auction as the auction is not valid.");
				return;
			}
			trader.addTrade(trade, false);
			//Delete the contents of the auctionItems
			this.auctionItems.clearContent();
			//Send response message to the client
			this.menu.SendMessage(LazyPacketData.simpleBoolean("AuctionCreated", true));
			for(SimpleSlot slot : this.slots) slot.locked = true;
			//LightmansCurrency.LogInfo("Successfully created the auction!");
		}
	}
	
	@Override
	public void receiveMessage(LazyPacketData message)
	{
		if(message.contains("CreateAuction"))
		{
			//LightmansCurrency.LogInfo("Received Auction from the client.\n" + message.getCompound("CreateAuction").getAsString());
			this.createAuction(new AuctionTradeData(message.getNBT("CreateAuction")));
		}
	}
	
}
