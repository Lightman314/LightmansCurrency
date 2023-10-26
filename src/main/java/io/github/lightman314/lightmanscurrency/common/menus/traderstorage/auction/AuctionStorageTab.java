package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.auction;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction.AuctionStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionPlayerStorage;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AuctionStorageTab extends TraderStorageTab {

	public AuctionStorageTab(TraderStorageMenu menu) { super(menu); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(Object screen) { return new AuctionStorageClientTab(screen, this); }
	
	@Override
	public boolean canOpen(Player player) { return true; }
	
	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }
	
	@Override
	public void onTabOpen() { }
	
	@Override
	public void onTabClose() { }
	
	public void clickedOnSlot(int storageSlot, boolean isShiftHeld) 
	{
		if(this.menu.getTrader() instanceof AuctionHouseTrader trader)
		{
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
				this.menu.SendMessage(LazyPacketData.builder()
						.setInt("ClickedSlot", storageSlot)
						.setBoolean("HeldShift", isShiftHeld));
			}
		}
	}
	
	public void quickTransfer() {
		if(this.menu.getTrader() instanceof AuctionHouseTrader trader)
		{
			AuctionPlayerStorage storage = trader.getStorage(this.menu.player);
			storage.collectItems(this.menu.player);
			trader.markStorageDirty();
			
			if(this.menu.isClient())
				this.menu.SendMessage(LazyPacketData.simpleFlag("QuickTransfer"));
		}
	}
	
	public void collectCoins() {
		if(this.menu.getTrader() instanceof AuctionHouseTrader trader)
		{
			AuctionPlayerStorage storage = trader.getStorage(this.menu.player);
			storage.collectedMoney(this.menu.player);
			trader.markStorageDirty();
			
			if(this.menu.isClient())
				this.menu.SendMessage(LazyPacketData.simpleFlag("CollectMoney"));
		}
	}
	
	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("ClickedSlot", LazyPacketData.TYPE_INT))
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
