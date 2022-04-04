package io.github.lightman314.lightmanscurrency.menus.traderstorage.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.item.ItemStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.UpgradeInputSlot;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.common.TraderItemStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemStorageTab extends TraderStorageTab{

	public ItemStorageTab(TraderStorageMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new ItemStorageClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return this.menu.getTrader() instanceof IItemTrader; }
	
	//Eventually will add upgrade slots
	List<SimpleSlot> slots = new ArrayList<>();
	public List<? extends Slot> getSlots() { return this.slots; }
	
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
		//Upgrade Slots
		if(this.menu.getTrader() instanceof IItemTrader)
		{
			IItemTrader trader = (IItemTrader)this.menu.getTrader();
			for(int i = 0; i < trader.getUpgradeInventory().getContainerSize(); ++i)
			{
				SimpleSlot upgradeSlot = new UpgradeInputSlot(trader.getUpgradeInventory(), i, 176, 18 + 18 * i, trader, this::onUpgradeModified);
				upgradeSlot.active = false;
				addSlot.apply(upgradeSlot);
				this.slots.add(upgradeSlot);
			}
		}
	}

	private void onUpgradeModified() {
		if(this.menu.getTrader() instanceof IItemTrader) {
			((IItemTrader)this.menu.getTrader()).markUpgradesDirty();
		}
	}

	@Override
	public void onTabOpen() { SimpleSlot.SetActive(this.slots); }
	
	@Override
	public void onTabClose() { SimpleSlot.SetInactive(this.slots); }
	
	@Override
	public boolean quickMoveStack(ItemStack stack) {
		if(this.menu.getTrader() instanceof IItemTrader) {
			IItemTrader trader = (IItemTrader)this.menu.getTrader();
			TraderItemStorage storage = trader.getStorage();
			if(storage.getFittableAmount(stack) > 0)
			{
				storage.tryAddItem(stack);
				return true;
			}	
		}
		return super.quickMoveStack(stack);
	}
	
	public void clickedOnSlot(int storageSlot, boolean isShiftHeld, boolean leftClick) {
		if(this.menu.getTrader() instanceof IItemTrader)
		{
			IItemTrader trader = (IItemTrader)this.menu.getTrader();
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
					int removedAmount = 0;
					
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
		CompoundTag message = new CompoundTag();
		message.putInt("ClickedSlot", storageSlot);
		message.putBoolean("HeldShift", isShiftHeld);
		message.putBoolean("LeftClick", leftClick);
		this.menu.sendMessage(message);
	}
	
	@Override
	public void receiveMessage(CompoundTag message) { 
		if(message.contains("ClickedSlot", Tag.TAG_INT))
		{
			int storageSlot = message.getInt("ClickedSlot");
			boolean isShiftHeld = message.getBoolean("HeldShift");
			boolean leftClick = message.getBoolean("LeftClick");
			this.clickedOnSlot(storageSlot, isShiftHeld, leftClick);
		}
	}

}
