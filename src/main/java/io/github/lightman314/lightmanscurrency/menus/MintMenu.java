package io.github.lightman314.lightmanscurrency.menus;

import io.github.lightman314.lightmanscurrency.blockentity.CoinMintBlockEntity;
import io.github.lightman314.lightmanscurrency.core.ModMenus;
import io.github.lightman314.lightmanscurrency.menus.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.mint.MintSlot;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
public class MintMenu extends AbstractContainerMenu{

	public final CoinMintBlockEntity tileEntity;
	
	public MintMenu(int windowId, Inventory inventory, CoinMintBlockEntity tileEntity)
	{
		super(ModMenus.MINT.get(), windowId);
		this.tileEntity = tileEntity;
		
		//Slots
		this.addSlot(new MintSlot(this.tileEntity.getStorage(), 0, 56, 21, this.tileEntity));
		this.addSlot(new OutputSlot(this.tileEntity.getStorage(), 1, 116, 21));
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 56 + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, 8 + x * 18, 114));
		}
	}
	
	@Override
	public boolean stillValid(Player playerIn)
	{
		return true;
	}
	
	@Override
	public void removed(Player playerIn)
	{
		super.removed(playerIn);
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
			if(index < this.tileEntity.getStorage().getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack, this.tileEntity.getStorage().getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.moveItemStackTo(slotStack, 0, this.tileEntity.getStorage().getContainerSize() - 1, false))
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
	
	public boolean isMeltInput()
	{
		return MoneyUtil.isCoin(this.tileEntity.getStorage().getItem(0));
	}
	
}
