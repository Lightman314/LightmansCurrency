package io.github.lightman314.lightmanscurrency.menus.slots;

import java.util.function.Function;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class SimpleSlot extends Slot{

	public boolean active = true;
	
	public SimpleSlot(Container container, int index, int x, int y) { super(container, index, x, y); }

	@Override
	public boolean isActive() { return this.active; }
	
	public static void SetActive(AbstractContainerMenu menu) {
		SetActive(menu, (slot) -> true);
	}
	
	public static void SetActive(AbstractContainerMenu menu, Function<SimpleSlot,Boolean> filter) {
		menu.slots.forEach(slot -> {
			if(slot instanceof SimpleSlot) {
				SimpleSlot simpleSlot = (SimpleSlot)slot;
				if(filter.apply(simpleSlot))
					simpleSlot.active = true;
			}
		});
	}
	
	public static void SetInactive(AbstractContainerMenu menu) {
		SetInactive(menu, (slot) -> true);
	}
	
	public static void SetInactive(AbstractContainerMenu menu, Function<SimpleSlot,Boolean> filter) {
		menu.slots.forEach(slot -> {
			if(slot instanceof SimpleSlot) {
				SimpleSlot simpleSlot = (SimpleSlot)slot;
				if(filter.apply(simpleSlot))
					simpleSlot.active = false;
			}
		});
	}
	
}
