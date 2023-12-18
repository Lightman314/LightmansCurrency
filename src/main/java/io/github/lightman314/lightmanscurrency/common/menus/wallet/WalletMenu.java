package io.github.lightman314.lightmanscurrency.common.menus.wallet;

import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

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
				this.addInventorySlot(8 + x * 18, 32 + (y + getRowCount()) * 18, index);
			}
		}
		
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addInventorySlot(8 + x * 18, 90 + getRowCount() * 18, x);
		}
		
		//Coin Slots last as they may vary between client and server at times.
		this.addCoinSlots(18);
		
		this.addDummySlots(37 + getMaxWalletSlots());
		
	}
	
	@Nonnull
	@Override
	public ItemStack quickMoveStack(@Nonnull Player playerEntity, int index)
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
	
	public void QuickCollectCoins()
	{
		Inventory inv = this.player.getInventory();
		for(int i = 0; i < inv.getContainerSize(); ++i)
		{
			ItemStack item = inv.getItem(i);
			if(CoinAPI.isCoin(item, false))
			{
				ItemStack result = this.PickupCoins(item);
				inv.setItem(i, result);
			}
		}
	}
	
}
