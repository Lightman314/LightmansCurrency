package io.github.lightman314.lightmanscurrency.common.menus.slots;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class OutputSlot extends SimpleSlot{
	
	public OutputSlot(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
        return false;
	}

}
