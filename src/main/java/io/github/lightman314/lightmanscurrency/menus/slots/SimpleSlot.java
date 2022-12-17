package io.github.lightman314.lightmanscurrency.menus.slots;

import java.util.List;
import java.util.function.Function;

import com.mojang.datafixers.types.Func;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SimpleSlot extends Slot{

	public boolean active = true;
	public boolean locked = false;
	
	public SimpleSlot(Container container, int index, int x, int y) { super(container, index, x, y); }

	@Override
	public boolean isActive() { return this.active; }
	
	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
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
	public @NotNull ItemStack remove(int amount) {
		if(this.locked)
			return ItemStack.EMPTY;
		return super.remove(amount);
	}
	
	@Override
	public boolean mayPickup(@NotNull Player player) {
		if(this.locked)
			return false;
		return super.mayPickup(player);
	}
	
	public static void SetActive(AbstractContainerMenu menu) {
		SetActive(menu, (slot) -> true);
	}
	
	public static void SetActive(AbstractContainerMenu menu, Function<SimpleSlot,Boolean> filter) {
		menu.slots.forEach(slot -> {
			if(slot instanceof SimpleSlot simpleSlot) {
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
			if(slot instanceof SimpleSlot simpleSlot) {
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

	public static void SetLocked(AbstractContainerMenu menu, boolean locked) { SetLocked(menu, locked, (slot) -> true); }

	public static void SetLocked(AbstractContainerMenu menu, boolean locked, Function<SimpleSlot,Boolean> filter) {
		menu.slots.forEach(slot -> {
			if(slot instanceof  SimpleSlot simpleSlot)
			{
				if(filter.apply(simpleSlot))
					simpleSlot.locked = true;
			}
		});
	}

	public static void Lock(AbstractContainerMenu menu) { SetLocked(menu, true); }

	public static void Lock(AbstractContainerMenu menu, Function<SimpleSlot,Boolean> filter) { SetLocked(menu, true, filter); }

	public static void Unlock(AbstractContainerMenu menu) { SetLocked(menu, false); }

	public static void Unlock(AbstractContainerMenu menu, Function<SimpleSlot,Boolean> filter) { SetLocked(menu, false, filter); }
	
}
