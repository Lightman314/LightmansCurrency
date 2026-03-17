package io.github.lightman314.lightmanscurrency.common.traders.auction.tabs;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageNodeTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.client.tabs.AuctionStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionPlayerStorage;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.nodes.AuctionStorageNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class AuctionStorageTab extends TraderStorageNodeTab<AuctionStorageNode> {

    public static final ResourceLocation KEY = LightmansCurrency.id("auction_personal_storage");

	public AuctionStorageTab(ITraderStorageMenu menu) { super(AuctionStorageNode.TYPE,menu); }

    @Override
    public ResourceLocation tabKey() { return KEY; }

    @Override
	public Object createClientTab(Object screen) { return new AuctionStorageClientTab(screen, this); }
	
	public void clickedOnSlot(int storageSlot, boolean isShiftHeld) 
	{
        AuctionStorageNode node = this.getNode();
		if(node != null)
		{
			AuctionPlayerStorage storage = node.getStorage(this.menu.getPlayer());
			if(storageSlot >= 0 && storageSlot < storage.getStoredItems().size())
			{
				ItemStack storedItem = storage.getStoredItems().get(storageSlot);
				if(storedItem.isEmpty())
				{
					storage.getStoredItems().remove(storageSlot);
                    storage.setChanged();
				}
				else
				{
					ItemStack heldItem = this.menu.getHeldItem();
					if(isShiftHeld)
					{
						//Move as much of the stored item from the slot into the players inventory
						this.menu.getPlayer().getInventory().add(storedItem);
						if(storedItem.isEmpty())
							storage.getStoredItems().remove(storageSlot);
						storage.setChanged();
					}
					else if(heldItem.isEmpty())
					{
						this.menu.setHeldItem(storedItem);
						storage.getStoredItems().remove(storageSlot);
						storage.setChanged();
					}
					else if(ItemStack.isSameItemSameComponents(storedItem, heldItem))
					{
						int transferCount = Math.min(heldItem.getMaxStackSize() - heldItem.getCount(), storedItem.getCount());
						if(transferCount > 0)
						{
							//Add to the held item
							heldItem.grow(transferCount);
							this.menu.setHeldItem(heldItem);
							//Shrink the storage count
							storedItem.shrink(transferCount);
							if(storedItem.isEmpty())
								storage.getStoredItems().remove(storageSlot);
							storage.setChanged();
						}
					}
				}
			}
			if(this.menu.isClient())
			{
				this.menu.SendMessage(this.builder()
						.setInt("ClickedSlot", storageSlot)
						.setBoolean("HeldShift", isShiftHeld));
			}
		}
	}
	
	public void quickTransfer() {
        AuctionStorageNode node = this.getNode();
		if(node != null)
		{
			AuctionPlayerStorage storage = node.getStorage(this.menu.getPlayer());
			storage.collectItems(this.menu.getPlayer());
			storage.setChanged();
			
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setFlag("QuickTransfer"));
		}
	}
	
	public void collectCoins() {
        AuctionStorageNode node = this.getNode();
		if(node != null)
		{
			AuctionPlayerStorage storage = node.getStorage(this.menu.getPlayer());
			storage.collectedMoney(this.menu.getPlayer());
			storage.setChanged();
			
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setFlag("CollectMoney"));
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
