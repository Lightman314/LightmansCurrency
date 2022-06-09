package io.github.lightman314.lightmanscurrency.menus.traderstorage.auction;

import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction.AuctionStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.universal_traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.universal_traders.auction.AuctionPlayerStorage;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AuctionStorageTab extends TraderStorageTab {

	public AuctionStorageTab(TraderStorageMenu menu) { super(menu); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new AuctionStorageClientTab(screen, this); }
	
	@Override
	public boolean canOpen(Player player) { return this.menu.getTrader() instanceof AuctionHouseTrader; }
	
	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }
	
	@Override
	public void onTabOpen() { }
	
	@Override
	public void onTabClose() { }
	
	public void clickedOnSlot(int storageSlot, boolean isShiftHeld) 
	{
		ITrader t = this.menu.getTrader();
		if(t instanceof AuctionHouseTrader)
		{
			AuctionHouseTrader trader = (AuctionHouseTrader)t;
			AuctionPlayerStorage storage = trader.getStorage(this.menu.player);
			ItemStack heldItem = this.menu.getCarried();
			if(heldItem.isEmpty())
			{
				//Move item out of storage
				List<ItemStack> storageContents = storage.getStoredItems();
				if(storageSlot >= 0 && storageSlot < storageContents.size())
				{
					ItemStack stackToRemove = storageContents.get(storageSlot).copy();
					
					//Assume we're moving a whole stack for now
					int tempAmount = Math.min(stackToRemove.getMaxStackSize(), stackToRemove.getCount());
					stackToRemove.setCount(tempAmount);
					int removedAmount = 0;
					
					if(isShiftHeld)
					{
						//Put the item in the players inventory. Will not throw overflow on the ground, so it will safely stop if the players inventory is full
						this.menu.player.getInventory().add(stackToRemove);
						//Determine the amount actually added to the players inventory
						removedAmount = tempAmount - stackToRemove.getCount();
					}
					else if(this.menu.getCarried().isEmpty())
					{
						//Put the item into the players hand
						this.menu.setCarried(stackToRemove);
						removedAmount = tempAmount;
					}
					//Remove the correct amount from storage
					if(removedAmount > 0)
					{
						LightmansCurrency.LogInfo("Removed " + removedAmount + "x " + stackToRemove.getItem().getRegistryName().toString() + " from storage.");
						storage.removePartial(storageSlot, removedAmount);
						//Mark the storage dirty
						trader.markStorageDirty();
					}
					else
						LightmansCurrency.LogInfo("Removed nothing from storage.");
				}
			}
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putInt("ClickedSlot", storageSlot);
				message.putBoolean("HeldShift", isShiftHeld);
				this.menu.sendMessage(message);
			}
		}
	}
	
	public void quickTransfer() {
		ITrader t = this.menu.getTrader();
		if(t instanceof AuctionHouseTrader)
		{
			AuctionHouseTrader trader = (AuctionHouseTrader)t;
			AuctionPlayerStorage storage = trader.getStorage(this.menu.player);
			storage.collectItems(this.menu.player);
			trader.markStorageDirty();
			
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putBoolean("QuickTransfer", true);
				this.menu.sendMessage(message);
			}
		}
	}
	
	public void collectCoins() {
		ITrader t = this.menu.getTrader();
		if(t instanceof AuctionHouseTrader)
		{
			AuctionHouseTrader trader = (AuctionHouseTrader)t;
			AuctionPlayerStorage storage = trader.getStorage(this.menu.player);
			storage.collectedMoney(this.menu.player);
			trader.markStorageDirty();
			
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putBoolean("CollectMoney", true);
				this.menu.sendMessage(message);
			}
		}
	}
	
	@Override
	public void receiveMessage(CompoundTag message) {
		if(message.contains("ClickedSlot", Tag.TAG_INT))
		{
			int storageSlot = message.getInt("ClickedSlot");
			boolean isShiftHeld = message.getBoolean("HeldShift");
			this.clickedOnSlot(storageSlot, isShiftHeld);
		}
		if(message.contains("QuickTransfer"))
		{
			this.quickTransfer();
		}
		if(message.contains("CollectMoney"))
		{
			this.collectCoins();
		}
	}
	
}
