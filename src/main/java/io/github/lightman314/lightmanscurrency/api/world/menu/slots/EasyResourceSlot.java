package io.github.lightman314.lightmanscurrency.api.world.menu.slots;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import javax.annotation.Nullable;
import java.util.List;

public class EasyResourceSlot extends ResourceHandlerSlot implements IEasySlot {

    private boolean active = true;
    private boolean locked = false;

    @Nullable
    private List<Identifier> background = null;

    public EasyResourceSlot(ResourceHandler<ItemResource> handler,IndexModifier<ItemResource> slotModifier,int handlerSlot,int x,int y) {
        super(handler,slotModifier,handlerSlot,x,y);
    }
    public <T extends ResourceHandler<ItemResource> & IndexModifier<ItemResource>> EasyResourceSlot(T handler,int handlerSlot,int x,int y) {
        this(handler,handler,handlerSlot,x,y);
    }

    @Override
    public boolean isActive() { return this.active; }
    @Override
    public final void setActive(boolean active) { this.active = active; }
    @Override
    public final boolean isLocked() { return this.locked; }
    @Override
    public final void setLocked(boolean locked) { this.locked = locked; }
    @Override
    public void setBackground(List<Identifier> background) { this.background = background; }
    @Override
    @Nullable
    public Identifier getNoItemIcon() {
        if(this.background != null)
            return IEasySlot.getNoItemIcon(this.background);
        return super.getNoItemIcon();
    }

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

    public static class OutputOnly extends EasyResourceSlot {
        public OutputOnly(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier, int handlerSlot, int x, int y) {
            super(handler, slotModifier, handlerSlot, x, y);
        }
        public <T extends ResourceHandler<ItemResource> & IndexModifier<ItemResource>> OutputOnly(T handler, int handlerSlot, int x, int y) {
            super(handler, handlerSlot, x, y);
        }
        @Override
        public boolean mayPlace(ItemStack stack) { return false; }
    }

}
