package io.github.lightman314.lightmanscurrency.containers.slots;

import java.util.function.Function;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;

public class SimpleSlot extends Slot{

	public boolean active = true;
	
	public SimpleSlot(IInventory inventory, int index, int x, int y) { super(inventory, index, x, y); }

	@Override
	public boolean isEnabled() { return this.active; }
	
	public static void SetActive(Container menu) {
		SetActive(menu, (slot) -> true);
	}

	public static void SetActive(Container menu, Function<SimpleSlot,Boolean> filter) {
		menu.inventorySlots.forEach(slot -> {
			if(slot instanceof SimpleSlot) {
				SimpleSlot simpleSlot = (SimpleSlot)slot;
				if(filter.apply(simpleSlot))
					simpleSlot.active = true;
			}
		});
	}

	public static void SetInactive(Container menu) {
		SetInactive(menu, (slot) -> true);
	}

	public static void SetInactive(Container menu, Function<SimpleSlot,Boolean> filter) {
		menu.inventorySlots.forEach(slot -> {
			if(slot instanceof SimpleSlot) {
				SimpleSlot simpleSlot = (SimpleSlot)slot;
				if(filter.apply(simpleSlot))
					simpleSlot.active = false;
			}
		});
	}
	
}
