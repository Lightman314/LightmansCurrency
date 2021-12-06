package io.github.lightman314.lightmanscurrency.menus.slots;

import io.github.lightman314.lightmanscurrency.blockentity.CoinMintBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MintSlot extends Slot{
	
	CoinMintBlockEntity tileEntity;
	
	public MintSlot(Container inventory, int index, int x, int y, CoinMintBlockEntity tileEntity)
	{
		super(inventory, index, x, y);
		this.tileEntity = tileEntity;
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
        return this.tileEntity.validMintInput(stack);
	}

}
