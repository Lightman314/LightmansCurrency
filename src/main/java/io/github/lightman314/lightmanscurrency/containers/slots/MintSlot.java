package io.github.lightman314.lightmanscurrency.containers.slots;

import io.github.lightman314.lightmanscurrency.tileentity.CoinMintTileEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class MintSlot extends Slot{
	
	CoinMintTileEntity tileEntity;
	
	public MintSlot(IInventory inventory, int index, int x, int y, CoinMintTileEntity tileEntity)
	{
		super(inventory, index, x, y);
		this.tileEntity = tileEntity;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
        return this.tileEntity.validMintInput(stack);
	}

}
