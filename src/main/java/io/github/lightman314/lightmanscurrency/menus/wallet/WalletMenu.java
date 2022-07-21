package io.github.lightman314.lightmanscurrency.menus.wallet;

import io.github.lightman314.lightmanscurrency.core.ModMenus;
import io.github.lightman314.lightmanscurrency.menus.slots.BlacklistSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.DisplaySlot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WalletMenu extends WalletMenuBase {
	
	public WalletMenu(int windowId, Inventory inventory, int walletStackIndex)
	{
		
		super(ModMenus.WALLET.get(), windowId, inventory, walletStackIndex);
		
		//Player Inventory before coin slots for desync safety.
		//Should make the Player Inventory slot indexes constant regardless of the wallet state.
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				int index = x + (y * 9) + 9;
				if(index == this.walletStackIndex)
					this.addSlot(new DisplaySlot(this.inventory, index, 8 + x * 18, 32 + (y + getRowCount()) * 18));
				else
					this.addSlot(new BlacklistSlot(this.inventory, index, 8 + x * 18, 32 + (y + getRowCount()) * 18, this.inventory, this.walletStackIndex));
			}
		}
		
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			if(x == this.walletStackIndex)
				this.addSlot(new DisplaySlot(this.inventory, x, 8 + x * 18, 90 + getRowCount() * 18));
			else
				this.addSlot(new BlacklistSlot(this.inventory, x, 8 + x * 18, 90 + getRowCount() * 18, this.inventory, this.walletStackIndex));
		}
		
		//Coin Slots last as they may vary between client and server at times.
		this.addCoinSlots(18);
		
		this.addDummySlots(37 + getMaxWalletSlots());
		
	}
	
	@Override
	public ItemStack quickMoveStack(Player playerEntity, int index)
	{
		
		if(index + this.coinInput.getContainerSize() == this.walletStackIndex)
			return ItemStack.EMPTY;
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			if(index < 36)
			{
				if(!this.moveItemStackTo(slotStack, 36, this.slots.size(), false))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.moveItemStackTo(slotStack, 0, 36, true))
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
	
}
