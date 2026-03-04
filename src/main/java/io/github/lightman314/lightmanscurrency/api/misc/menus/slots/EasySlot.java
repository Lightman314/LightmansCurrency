package io.github.lightman314.lightmanscurrency.api.misc.menus.slots;

import java.util.List;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;

public class EasySlot extends SlotItemHandler {

    public static final ResourceLocation EMPTY_SLOT_BG = LightmansCurrency.id( "item/empty_item_slot");
    public static final Pair<ResourceLocation,ResourceLocation> BACKGROUND = Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_SLOT_BG);

    private static final Container emptyInventory = new SimpleContainer(0);

    public final IItemHandler itemHandler;

    public boolean active = true;
    public boolean locked = false;

    private Runnable listener = () ->{};

    //Public facing constructor needs IItemHandlerModifiable
    public EasySlot(IItemHandlerModifiable itemHandler, int index, int x, int y) { super(itemHandler,index,x,y); this.itemHandler = itemHandler; }
    //Protected constructor allows non-modifiable IItemHandler input under the assumption that they'll override the relevant set methods
    protected EasySlot(IItemHandler itemHandler, int index, int x, int y) { super(itemHandler,index,x,y); this.itemHandler = itemHandler; }

    @Override
    public boolean isActive() { return this.active; }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if(this.locked || !this.active)
            return false;
        return super.mayPlace(stack);
    }

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

    public final void setListener(Runnable listener) { this.listener = listener; }

    @Override
    public void setChanged() {
        super.setChanged();
        this.listener.run();
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
                    simpleSlot.locked = locked;
            }
        });
    }

    public static void Lock(AbstractContainerMenu menu) { SetLocked(menu, true); }

    public static void Lock(AbstractContainerMenu menu, Function<EasySlot,Boolean> filter) { SetLocked(menu, true, filter); }

    public static void Unlock(AbstractContainerMenu menu) { SetLocked(menu, false); }

    public static void Unlock(AbstractContainerMenu menu, Function<EasySlot,Boolean> filter) { SetLocked(menu, false, filter); }

}