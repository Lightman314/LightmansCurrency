package io.github.lightman314.lightmanscurrency.menus.slots;

import java.util.List;
import java.util.function.Function;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SimpleSlot extends Slot{

	public boolean active = true;
	public boolean locked = false;
	
	public SimpleSlot(Container container, int index, int x, int y) { super(container, index, x, y); }

	@Override
	public boolean isActive() { return this.active; }
	
	@Override
	public boolean mayPlace(ItemStack stack) {
		if(this.locked)
			return false;
		return super.mayPlace(stack);
	}
	
	//Don't override set as it's used for server-client sync
	/*@Override
	public void set(ItemStack stack) {
		if(this.locked)
			return;
		super.set(stack);
	}*/
	
	@Override
	public ItemStack remove(int amount) {
		if(this.locked)
			return ItemStack.EMPTY;
		return super.remove(amount);
	}
	
	@Override
	public boolean mayPickup(Player player) {
		if(this.locked)
			return false;
		return super.mayPickup(player);
	}
	
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
	
	public static void SetActive(List<? extends SimpleSlot> slots) { SetActive(slots, true); }
	public static void SetInactive(List<? extends SimpleSlot> slots) { SetActive(slots, false); }
	
	public static void SetActive(List<? extends SimpleSlot> slots, boolean active) {
		for(SimpleSlot slot: slots) {
			slot.active = active;
		}
	}
	
}
