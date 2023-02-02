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
import org.jetbrains.annotations.NotNull;

public class MintMenu extends AbstractContainerMenu{

	public final CoinMintBlockEntity blockEntity;
	
	public MintMenu(int windowId, Inventory inventory, CoinMintBlockEntity blockEntity)
	{
		super(ModMenus.MINT.get(), windowId);
		this.blockEntity = blockEntity;
		
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
	public boolean stillValid(@NotNull Player playerIn)
	{
		return true;
	}
	
	@Override
	public void removed(@NotNull Player playerIn)
	{
		super.removed(playerIn);
	}
	
	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player playerEntity, int index)
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
