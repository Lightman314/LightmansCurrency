package io.github.lightman314.lightmanscurrency.menus.traderstorage.auction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction.AuctionCreateClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.auction.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AuctionCreateTab extends TraderStorageTab {
	
	public AuctionCreateTab(TraderStorageMenu menu) { super(menu); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new AuctionCreateClientTab(screen, this); }
	
	@Override
	public boolean canOpen(Player player) { return this.menu.getTrader() instanceof AuctionHouseTrader; }
	
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
		TraderData t = this.menu.getTrader();
		if(t instanceof AuctionHouseTrader)
		{
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.put("CreateAuction", trade.getAsNBT());
				this.menu.sendMessage(message);
				return;
			}
			//Set the trade's auction items based on the items currently in the auction item slots
			trade.setAuctionItems(this.auctionItems);
			if(!trade.isValid())
			{
				//Send failure message to the client.
				CompoundTag message = new CompoundTag();
				message.putBoolean("AuctionCreated", false);
				this.menu.sendMessage(message);
				//LightmansCurrency.LogInfo("Failed to create the auction as the auction is not valid.");
				return;
			}	
			AuctionHouseTrader trader = (AuctionHouseTrader)t;
			trader.addTrade(trade, false);
			//Delete the contents of the auctionItems
			this.auctionItems.clearContent();
			//Send response message to the client
			CompoundTag message = new CompoundTag();
			message.putBoolean("AuctionCreated", true);
			this.menu.sendMessage(message);
			for(SimpleSlot slot : this.slots) slot.locked = true;
			//LightmansCurrency.LogInfo("Successfully created the auction!");
		}
	}
	
	@Override
	public void receiveMessage(CompoundTag message)
	{
		if(message.contains("CreateAuction"))
		{
			//LightmansCurrency.LogInfo("Received Auction from the client.\n" + message.getCompound("CreateAuction").getAsString());
			this.createAuction(new AuctionTradeData(message.getCompound("CreateAuction")));
		}
	}
	
}