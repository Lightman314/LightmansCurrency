package io.github.lightman314.lightmanscurrency.containers.slots;

import io.github.lightman314.lightmanscurrency.containers.MintContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class MintSlot extends Slot{
	
	MintContainer container;
	
	public MintSlot(IInventory inventory, int index, int x, int y, MintContainer container)
	{
		super(inventory, index, x, y);
		this.container = container;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
        return container.validMintInput(stack);
	}

}
