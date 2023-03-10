package io.github.lightman314.lightmanscurrency.common.menus.slots.mint;

import io.github.lightman314.lightmanscurrency.common.blockentity.CoinMintBlockEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class MintSlot extends Slot {
	
	CoinMintBlockEntity tileEntity;
	
	public MintSlot(IInventory inventory, int index, int x, int y, CoinMintBlockEntity tileEntity)
	{
		super(inventory, index, x, y);
		this.tileEntity = tileEntity;
	}
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
        return this.tileEntity.validMintInput(stack);
	}

}
