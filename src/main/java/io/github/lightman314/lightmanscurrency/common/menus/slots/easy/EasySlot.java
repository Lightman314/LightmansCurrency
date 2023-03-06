package io.github.lightman314.lightmanscurrency.common.menus.slots.easy;

import java.util.List;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class EasySlot extends Slot {

    public static final ResourceLocation EMPTY_SLOT_BG = new ResourceLocation(LightmansCurrency.MODID, "item/empty_item_slot");
    public static final Pair<ResourceLocation,ResourceLocation> BACKGROUND = Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_SLOT_BG);


    public boolean active = true;
    public boolean locked = false;

    public EasySlot(IInventory container, int index, int x, int y) { super(container, index, x, y); }

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

    public static void SetActive(Container menu, Function<EasySlot,Boolean> filter) {
        menu.slots.forEach(slot -> {
            if(slot instanceof EasySlot) {
                EasySlot simpleSlot = (EasySlot)slot;
                if(filter.apply(simpleSlot))
                    simpleSlot.active = true;
            }
        });
    }

    public static void SetInactive(Container menu) {
        SetInactive(menu, (slot) -> true);
    }

    public static void SetInactive(Container menu, Function<EasySlot,Boolean> filter) {
        menu.slots.forEach(slot -> {
            if(slot instanceof EasySlot) {
                EasySlot simpleSlot = (EasySlot)slot;
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

    public static void SetLocked(Container menu, boolean locked) { SetLocked(menu, locked, (slot) -> true); }

    public static void SetLocked(Container menu, boolean locked, Function<EasySlot,Boolean> filter) {
        menu.slots.forEach(slot -> {
            if(slot instanceof EasySlot)
            {
                EasySlot simpleSlot = (EasySlot)slot;
                if(filter.apply(simpleSlot))
                    simpleSlot.locked = locked;
            }
        });
    }

    public static void Lock(Container menu) { SetLocked(menu, true); }

    public static void Lock(Container menu, Function<EasySlot,Boolean> filter) { SetLocked(menu, true, filter); }

    public static void Unlock(Container menu) { SetLocked(menu, false); }

    public static void Unlock(Container menu, Function<EasySlot,Boolean> filter) { SetLocked(menu, false, filter); }

}