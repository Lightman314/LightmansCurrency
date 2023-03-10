package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.auction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction.AuctionCreateClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AuctionCreateTab extends TraderStorageTab {
	
	public AuctionCreateTab(TraderStorageMenu menu) { super(menu); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new AuctionCreateClientTab(screen, this); }
	
	@Override
	public boolean canOpen(PlayerEntity player) { return this.menu.getTrader() instanceof AuctionHouseTrader; }
	
	List<SimpleSlot> slots = new ArrayList<>();
	public List<SimpleSlot> getSlots() { return this.slots; }
	Inventory auctionItems = new Inventory(2);
	public Inventory getAuctionItems() { return this.auctionItems; }
	
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
				CompoundNBT message = new CompoundNBT();
				message.put("CreateAuction", trade.getAsNBT());
				this.menu.sendMessage(message);
				return;
			}
			//Set the trade's auction items based on the items currently in the auction item slots
			trade.setAuctionItems(this.auctionItems);
			if(!trade.isValid())
			{
				//Send failure message to the client.
				CompoundNBT message = new CompoundNBT();
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
			CompoundNBT message = new CompoundNBT();
			message.putBoolean("AuctionCreated", true);
			this.menu.sendMessage(message);
			for(SimpleSlot slot : this.slots) slot.locked = true;
			//LightmansCurrency.LogInfo("Successfully created the auction!");
		}
	}
	
	@Override
	public void receiveMessage(CompoundNBT message)
	{
		if(message.contains("CreateAuction"))
		{
			//LightmansCurrency.LogInfo("Received Auction from the client.\n" + message.getCompound("CreateAuction").getAsString());
			this.createAuction(new AuctionTradeData(message.getCompound("CreateAuction")));
		}
	}
	
}