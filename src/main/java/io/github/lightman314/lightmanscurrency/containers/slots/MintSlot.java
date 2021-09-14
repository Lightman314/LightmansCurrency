package io.github.lightman314.lightmanscurrency.containers.slots;

import io.github.lightman314.lightmanscurrency.containers.MintContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MintSlot extends Slot{
	
	MintContainer container;
	
	public MintSlot(Container inventory, int index, int x, int y, MintContainer container)
	{
		super(inventory, index, x, y);
		this.container = container;
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
        return container.validMintInput(stack);
	}

}
