package io.github.lightman314.lightmanscurrency.menus.slots.easy;

import java.util.List;
import java.util.function.Function;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EasySlot extends Slot {

    public boolean active = true;
    public boolean locked = false;

    public EasySlot(Container container, int index, int x, int y) { super(container, index, x, y); }

    @Override
    public boolean isActive() { return this.active; }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if(this.locked)
            return false;
        return super.mayPlace(stack);
    }

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

    public static void SetActive(AbstractContainerMenu menu, Function<EasySlot,Boolean> filter) {
        menu.slots.forEach(slot -> {
            if(slot instanceof EasySlot simpleSlot) {
                if(filter.apply(simpleSlot))
                    simpleSlot.active = true;
            }
        });
    }

    public static void SetInactive(AbstractContainerMenu menu) {
        SetInactive(menu, (slot) -> true);
    }

    public static void SetInactive(AbstractContainerMenu menu, Function<EasySlot,Boolean> filter) {
        menu.slots.forEach(slot -> {
            if(slot instanceof EasySlot simpleSlot) {
                if(filter.apply(simpleSlot))
                    simpleSlot.active = false;
            }
        });
    }

    public static void SetActive(List<? extends EasySlot> slots) { SetActive(slots, true); }
    public static void SetInactive(List<? extends EasySlot> slots) { SetActive(slots, false); }

    public static void SetActive(List<? extends EasySlot> slots, boolean active) {
        for(EasySlot slot: slots) {
            slot.active = active;
        }
    }

    public static void SetLocked(AbstractContainerMenu menu, boolean locked) { SetLocked(menu, locked, (slot) -> true); }

    public static void SetLocked(AbstractContainerMenu menu, boolean locked, Function<EasySlot,Boolean> filter) {
        menu.slots.forEach(slot -> {
            if(slot instanceof  EasySlot simpleSlot)
            {
                if(filter.apply(simpleSlot))
                    simpleSlot.locked = true;
            }
        });
    }

    public static void Lock(AbstractContainerMenu menu) { SetLocked(menu, true); }

    public static void Lock(AbstractContainerMenu menu, Function<EasySlot,Boolean> filter) { SetLocked(menu, true, filter); }

    public static void Unlock(AbstractContainerMenu menu) { SetLocked(menu, false); }

    public static void Unlock(AbstractContainerMenu menu, Function<EasySlot,Boolean> filter) { SetLocked(menu, false, filter); }

}