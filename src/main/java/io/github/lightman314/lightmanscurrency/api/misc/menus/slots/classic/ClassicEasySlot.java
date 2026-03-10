package io.github.lightman314.lightmanscurrency.api.misc.menus.slots.classic;

import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.IEasySlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ClassicEasySlot extends Slot implements IEasySlot {

    private boolean active = true;
    @Override
    public boolean isActive() { return this.active; }
    private boolean locked = false;
    @Override
    public boolean isLocked() { return this.locked; }

    private Runnable listener = () ->{};

    public ClassicEasySlot(Container container, int slot, int x, int y) { super(container,slot,x,y); }

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
    public void setLocked(boolean locked) { this.locked = locked;}
    @Override
    public void setActive(boolean active) { this.active = active; }

    @Override
    public void setChanged() {
        super.setChanged();
        this.listener.run();
    }

}
