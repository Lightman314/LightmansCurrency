package io.github.lightman314.lightmanscurrency.menus;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.client.gui.widget.lockableslot.LockableSlotInterface;
import io.github.lightman314.lightmanscurrency.client.gui.widget.lockableslot.LockableSlotInterface.ILockableSlotInteractableMenu;
import io.github.lightman314.lightmanscurrency.core.ModMenus;
import io.github.lightman314.lightmanscurrency.menus.containers.LockableContainer.LockData;
import io.github.lightman314.lightmanscurrency.menus.slots.LockableSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ItemInterfaceMenu extends AbstractContainerMenu implements ILockableSlotInteractableMenu{

	public final UniversalItemTraderInterfaceBlockEntity blockEntity;
	
	public final Player player;
	
	public ItemInterfaceMenu(int windowID, Inventory inventory, UniversalItemTraderInterfaceBlockEntity blockEntity) {
		super(ModMenus.ITEM_INTERFACE, windowID);
		
		this.player = inventory.player;
		this.blockEntity = blockEntity;
		
		//Item Buffer
		for(int x = 0; x < this.blockEntity.getItemBuffer().getSlots(); x++)
		{
			this.addSlot(new LockableSlot(this.blockEntity.getItemBuffer(), x, 8 + x * 18, 98));
		}
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 130 + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, 8 + x * 18, 188));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return !this.blockEntity.isRemoved() && this.blockEntity.isOwner(player);
	}
	
	@Override
	public ItemStack quickMoveStack(Player playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			if(index < this.blockEntity.getItemBuffer().getSlots())
			{
				if(!this.moveItemStackTo(slotStack,  this.blockEntity.getItemBuffer().getSlots(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.moveItemStackTo(slotStack, 0, this.blockEntity.getItemBuffer().getSlots(), false))
			{
				return ItemStack.EMPTY;
			}
			
			if(slotStack.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else
			{
				slot.setChanged();
			}
		}
		
		return clickedStack;
		
	}
	
	/**
	 * Interacts with the lock data for the given slot.
	 * @param lockSlot The index of the lock slot to interact with.
	 * @param carriedStack The clients carried stack. Only used if the player is in creative mode. Otherwise the true carried stack will be used.
	 */
	public void OnLockableSlotInteraction(String key, int index, ItemStack carriedStack) {
		ItemStack interactionStack = this.player.isCreative() ? carriedStack : this.getCarried();
		if(key.contentEquals(LockableSlotInterface.DEFAULT_KEY))
		{
			LockData data = this.blockEntity.getItemBuffer().getLockData(index);
			if(data != null)
			{
				if(!data.hasItemFilter() && interactionStack.isEmpty())
					data.setFullyLocked(!data.fullyLocked());
				else
					data.setFilter(interactionStack);
				this.blockEntity.setItemBufferDirty();
			}
		}
	}
	
	
	
}
