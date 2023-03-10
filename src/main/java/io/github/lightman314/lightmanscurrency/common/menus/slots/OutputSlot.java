package io.github.lightman314.lightmanscurrency.common.menus.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class OutputSlot extends SimpleSlot{
	
	public OutputSlot(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
        return false;
	}

}
