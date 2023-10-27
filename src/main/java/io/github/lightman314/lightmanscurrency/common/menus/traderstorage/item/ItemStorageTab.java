package io.github.lightman314.lightmanscurrency.common.menus.traderstorage.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.item.ItemStorageClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.network.packet.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemStorageTab extends TraderStorageTab{

	public ItemStorageTab(TraderStorageMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(Object screen) { return new ItemStorageClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return true; }
	
	List<SimpleSlot> slots = new ArrayList<>();
	public List<? extends Slot> getSlots() { return this.slots; }
	
	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
		//Upgrade Slots
		if(this.menu.getTrader() instanceof ItemTraderData trader && !trader.isPersistent())
		{
			for(int i = 0; i < trader.getUpgrades().getContainerSize(); ++i)
			{
				SimpleSlot upgradeSlot = new UpgradeInputSlot(trader.getUpgrades(), i, 176, 18 + 18 * i, trader);
				upgradeSlot.active = false;
				addSlot.apply(upgradeSlot);
				this.slots.add(upgradeSlot);
			}
		}
	}
	
	@Override
	public void onTabOpen() { SimpleSlot.SetActive(this.slots); }

	@Override
	public void onTabClose() { SimpleSlot.SetInactive(this.slots); }
	
	
	@Override
	public boolean quickMoveStack(ItemStack stack) {
		if(this.menu.getTrader() instanceof ItemTraderData trader) {
			if(trader.isPersistent())
				return false;
			TraderItemStorage storage = trader.getStorage();
			if(storage.getFittableAmount(stack) > 0)
			{
				storage.tryAddItem(stack);
				trader.markStorageDirty();
				return true;
			}	
		}
		return super.quickMoveStack(stack);
	}
	
	public void clickedOnSlot(int storageSlot, boolean isShiftHeld, boolean leftClick) {
		if(this.menu.getTrader() instanceof ItemTraderData trader)
		{
			if(trader.isPersistent())
				return;
			TraderItemStorage storage = trader.getStorage();
			ItemStack heldItem = this.menu.getCarried();
			if(heldItem.isEmpty())
			{
				//Move item out of storage
				List<ItemStack> storageContents = storage.getContents();
				if(storageSlot >= 0 && storageSlot < storageContents.size())
				{
					ItemStack stackToRemove = storageContents.get(storageSlot).copy();
					ItemStack removeStack = stackToRemove.copy();
					
					//Assume we're moving a whole stack for now
					int tempAmount = Math.min(stackToRemove.getMaxStackSize(), stackToRemove.getCount());
					stackToRemove.setCount(tempAmount);
					int removedAmount;
					
					//Right-click, attempt to cut the stack in half
					if(!leftClick)
					{
						if(tempAmount > 1)
							tempAmount = tempAmount / 2;
						stackToRemove.setCount(tempAmount);
					}
					
					if(isShiftHeld)
					{
						//Put the item in the players inventory. Will not throw overflow on the ground, so it will safely stop if the players inventory is full
						this.menu.player.getInventory().add(stackToRemove);
						//Determine the amount actually added to the players inventory
						removedAmount = tempAmount - stackToRemove.getCount();
					}
					else
					{
						//Put the item into the players hand
						this.menu.setCarried(stackToRemove);
						removedAmount = tempAmount;
					}
					//Remove the correct amount from storage
					if(removedAmount > 0)
					{
						removeStack.setCount(removedAmount);
						storage.removeItem(removeStack);
						//Mark the storage dirty
						trader.markStorageDirty();
					}
				}
			}
			else
			{
				//Move from hand to storage
				if(leftClick)
				{
					storage.tryAddItem(heldItem);
					//Mark the storage dirty
					trader.markStorageDirty();
				}
				else
				{
					//Right click, only attempt to add 1 from the hand
					ItemStack addItem = heldItem.copy();
					addItem.setCount(1);
					if(storage.addItem(addItem))
					{
						heldItem.shrink(1);
						if(heldItem.isEmpty())
							this.menu.setCarried(ItemStack.EMPTY);
					}
					//Mark the storage dirty
					trader.markStorageDirty();
				}
			}
			if(this.menu.isClient())
				this.sendStorageClickMessage(storageSlot, isShiftHeld, leftClick);
		}
	}
	
	private void sendStorageClickMessage(int storageSlot, boolean isShiftHeld, boolean leftClick) {
		this.menu.SendMessage(LazyPacketData.builder()
				.setInt("ClickedSlot", storageSlot)
				.setBoolean("HeldShift", isShiftHeld)
				.setBoolean("LeftClick", leftClick));
	}
	
	public void quickTransfer(int type) {
		if(this.menu.getTrader() instanceof ItemTraderData trader) {
			if(trader.isPersistent())
				return;
			TraderItemStorage storage = trader.getStorage();
			Inventory inv = this.menu.player.getInventory();
			boolean changed = false;
			if(type == 0)
			{
				//Quick Deposit
				for(int i = 0; i < 36; ++i)
				{
					ItemStack stack = inv.getItem(i);
					int fillAmount = storage.getFittableAmount(stack);
					if(fillAmount > 0)
					{
						//Remove the item from the players inventory
						ItemStack fillStack = inv.removeItem(i, fillAmount);
						//Put the item into storage
						storage.forceAddItem(fillStack);
					}
				}
			}
			else if(type == 1)
			{
				//Quick Extract
				List<ItemStack> itemList = InventoryUtil.copyList(storage.getContents());
				for(ItemStack stack : itemList)
				{
					boolean keepTrying = true;
					while(storage.getItemCount(stack) > 0 && keepTrying)
					{
						ItemStack transferStack = stack.copy();
						int transferCount = Math.min(storage.getItemCount(stack), stack.getMaxStackSize());
						transferStack.setCount(transferCount);
						//Attempt to move the stack into the players inventory
						int removedCount = InventoryUtil.safeGiveToPlayer(inv, transferStack);
						if(removedCount > 0)
						{
							changed = true;
							//Remove the transferred amount from storage
							ItemStack removeStack = stack.copy();
							removeStack.setCount(removedCount);
							storage.removeItem(removeStack);
						}
						else
							keepTrying = false;
					}
				}
			}
			
			if(changed)
				trader.markStorageDirty();

			if(this.menu.isClient())
				this.menu.SendMessage(LazyPacketData.simpleInt("QuickTransfer", type));
			
		}
	}
	
	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("ClickedSlot", LazyPacketData.TYPE_INT))
		{
			int storageSlot = message.getInt("ClickedSlot");
			boolean isShiftHeld = message.getBoolean("HeldShift");
			boolean leftClick = message.getBoolean("LeftClick");
			this.clickedOnSlot(storageSlot, isShiftHeld, leftClick);
		}
		if(message.contains("QuickTransfer"))
		{
			this.quickTransfer(message.getInt("QuickTransfer"));
		}
	}

}
