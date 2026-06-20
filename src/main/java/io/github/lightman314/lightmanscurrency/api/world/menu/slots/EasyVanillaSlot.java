package io.github.lightman314.lightmanscurrency.api.world.menu.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EasyVanillaSlot extends Slot implements IEasySlot {

    private boolean active = true;
    private boolean locked = false;
    public EasyVanillaSlot(Container container,int slot, int x, int y) { super(container, slot, x, y); }

    @Override
    public final boolean isActive() { return this.active; }
    @Override
    public final void setActive(boolean active) { this.active = active; }

    @Override
    public final boolean isLocked() { return this.locked; }
    @Override
    public final void setLocked(boolean locked) { this.locked = locked; }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        if(this.isLocked() || !this.isActive())
            return false;
        return super.mayPlace(itemStack);
    }

    @Override
    public boolean mayPickup(Player player) {
        if(this.isLocked() || !this.isActive())
            return false;
        return super.mayPickup(player);
    }

}
