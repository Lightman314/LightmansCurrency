package io.github.lightman314.lightmanscurrency.api.misc.menus.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DisplaySlot extends Slot {

    public DisplaySlot(Container container, int index, int x, int y) { super(container,index,x,y); }
    @Override
    public boolean mayPlace(ItemStack stack) { return false; }
    @Override
    public boolean mayPickup(Player playerIn) { return false; }

}
