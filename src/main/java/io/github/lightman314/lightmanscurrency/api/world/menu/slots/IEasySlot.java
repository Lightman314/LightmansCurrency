package io.github.lightman314.lightmanscurrency.api.world.menu.slots;

import com.google.common.base.Predicates;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface IEasySlot {

    boolean isActive();
    void setActive(boolean active);

    boolean isLocked();
    void setLocked(boolean locked);

    static void setActive(AbstractContainerMenu menu,boolean active) { setActive(menu,Predicates.alwaysTrue(),active); }
    static void setActive(AbstractContainerMenu menu, Predicate<IEasySlot> filter,boolean active) { setActive(menu.slots,filter,active); }
    static void setActive(List<Slot> slots,boolean active) { setActive(slots,Predicates.alwaysTrue(),active); }
    static void setActive(List<Slot> slots,Predicate<IEasySlot> filter,boolean active) {
        for(Slot s : new ArrayList<>(slots))
        {
            if(s instanceof IEasySlot easySlot && filter.test(easySlot))
                easySlot.setActive(active);
        }
    }

    static void setLocked(AbstractContainerMenu menu,boolean locked) { setLocked(menu,Predicates.alwaysTrue(),locked); }
    static void setLocked(AbstractContainerMenu menu, Predicate<IEasySlot> filter,boolean locked) { setLocked(menu.slots,filter,locked); }
    static void setLocked(List<Slot> slots,boolean locked) { setLocked(slots,Predicates.alwaysTrue(),locked); }
    static void setLocked(List<Slot> slots,Predicate<IEasySlot> filter,boolean locked) {
        for(Slot s : new ArrayList<>(slots))
        {
            if(s instanceof IEasySlot easySlot && filter.test(easySlot))
                easySlot.setLocked(locked);
        }
    }

}