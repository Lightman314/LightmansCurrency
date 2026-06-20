package io.github.lightman314.lightmanscurrency.api.world.menu.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class EasyResourceSlot extends ResourceHandlerSlot implements IEasySlot {

    private boolean active = true;
    private boolean locked = false;
    public EasyResourceSlot(ResourceHandler<ItemResource> handler,IndexModifier<ItemResource> slotModifier,int handlerSlot,int x,int y) {
        super(handler,slotModifier,handlerSlot,x,y);
    }

    @Override
    public final boolean isActive() { return this.active; }
    @Override
    public final void setActive(boolean active) { this.active = active; }
    @Override
    public final boolean isLocked() { return this.locked; }
    @Override
    public final void setLocked(boolean locked) { this.locked = locked; }
    @Override
    public boolean mayPlace(ItemStack stack) {
        if(!this.isActive() || this.isLocked())
            return false;
        return super.mayPlace(stack);
    }
    @Override
    public boolean mayPickup(Player player) {
        if(!this.isActive() || this.isLocked())
            return false;
        return super.mayPickup(player);
    }

}
