package io.github.lightman314.lightmanscurrency.common.menus.slots;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

public class SimpleSlot extends Slot {

	public boolean active = true;
	public boolean locked = false;

	public SimpleSlot(IInventory container, int index, int x, int y) { super(container, index, x, y); }

	@Override
	public boolean isActive() { return this.active; }

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		if(this.locked)
			return false;
		return super.mayPlace(stack);
	}

	@Override
	public @Nonnull ItemStack remove(int amount) {
		if(this.locked)
			return ItemStack.EMPTY;
		return super.remove(amount);
	}

	@Override
	public boolean mayPickup(@Nonnull PlayerEntity player) {
		if(this.locked)
			return false;
		return super.mayPickup(player);
	}

	public static void SetActive(Container menu) {
		SetActive(menu, (slot) -> true);
	}

	public static void SetActive(Container menu, Function<SimpleSlot,Boolean> filter) {
		menu.slots.forEach(slot -> {
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

	public static void SetLocked(Container menu, boolean locked) { SetLocked(menu, locked, (slot) -> true); }

	public static void SetLocked(Container menu, boolean locked, Function<SimpleSlot,Boolean> filter) {
		menu.slots.forEach(slot -> {
			if(slot instanceof  SimpleSlot)
			{
				SimpleSlot simpleSlot = (SimpleSlot)slot;
				if(filter.apply(simpleSlot))
					simpleSlot.locked = locked;
			}
		});
	}

	public static void Lock(Container menu) { SetLocked(menu, true); }

	public static void Lock(Container menu, Function<SimpleSlot,Boolean> filter) { SetLocked(menu, true, filter); }

	public static void Unlock(Container menu) { SetLocked(menu, false); }

	public static void Unlock(Container menu, Function<SimpleSlot,Boolean> filter) { SetLocked(menu, false, filter); }

}