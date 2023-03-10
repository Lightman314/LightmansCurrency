package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.common.blockentity.CoinMintBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.common.menus.slots.mint.MintSlot;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class MintMenu extends Container {

	public final CoinMintBlockEntity blockEntity;
	
	public MintMenu(int windowId, PlayerInventory inventory, CoinMintBlockEntity tileEntity)
	{
		super(ModMenus.MINT.get(), windowId);
		this.blockEntity = tileEntity;
		
		//Slots
		this.addSlot(new MintSlot(this.blockEntity.getStorage(), 0, 56, 21, this.blockEntity));
		this.addSlot(new OutputSlot(this.blockEntity.getStorage(), 1, 116, 21));
		
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
	public boolean stillValid(@Nonnull PlayerEntity playerIn)
	{
		return true;
	}
	
	@Override
	public void removed(@Nonnull PlayerEntity playerIn)
	{
		super.removed(playerIn);
	}
	
	@Override
	public @Nonnull ItemStack quickMoveStack(@Nonnull PlayerEntity playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			if(index < this.blockEntity.getStorage().getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack, this.blockEntity.getStorage().getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.moveItemStackTo(slotStack, 0, this.blockEntity.getStorage().getContainerSize() - 1, false))
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
		return MoneyUtil.isCoin(this.blockEntity.getStorage().getItem(0));
	}
	
}
