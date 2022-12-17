package io.github.lightman314.lightmanscurrency.menus.slots;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OutputSlot extends SimpleSlot{
	
	public OutputSlot(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
	}

}
