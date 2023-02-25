package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.auction;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction.AuctionStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionPlayerStorage;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
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
		TraderData t = this.menu.getTrader();
		if(t instanceof AuctionHouseTrader)
		{
			AuctionHouseTrader trader = (AuctionHouseTrader)t;
			AuctionPlayerStorage storage = trader.getStorage(this.menu.player);
			if(storageSlot >= 0 && storageSlot < storage.getStoredItems().size())
			{
				ItemStack storedItem = storage.getStoredItems().get(storageSlot);
				if(storedItem.isEmpty())
				{
					storage.getStoredItems().remove(storageSlot);
					trader.markStorageDirty();
				}
				else
				{
					ItemStack heldItem = this.menu.getCarried();
					if(isShiftHeld)
					{
						//Move as much of the stored item from the slot into the players inventory
						this.menu.player.getInventory().add(storedItem);
						if(storedItem.isEmpty())
							storage.getStoredItems().remove(storageSlot);
						trader.markStorageDirty();
					}
					else if(heldItem.isEmpty())
					{
						this.menu.setCarried(storedItem);
						storage.getStoredItems().remove(storageSlot);
						trader.markStorageDirty();
					}
					else if(InventoryUtil.ItemMatches(storedItem, heldItem))
					{
						int transferCount = Math.min(heldItem.getMaxStackSize() - heldItem.getCount(), storedItem.getCount());
						if(transferCount > 0)
						{
							//Add to the held item
							heldItem.grow(transferCount);
							this.menu.setCarried(heldItem);
							//Shrink the storage count
							storedItem.shrink(transferCount);
							if(storedItem.isEmpty())
								storage.getStoredItems().remove(storageSlot);
							trader.markStorageDirty();
						}
					}
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
		TraderData t = this.menu.getTrader();
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
		TraderData t = this.menu.getTrader();
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
