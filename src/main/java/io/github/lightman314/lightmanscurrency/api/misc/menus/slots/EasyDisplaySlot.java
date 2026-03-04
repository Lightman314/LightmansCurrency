package io.github.lightman314.lightmanscurrency.api.misc.menus.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class EasyDisplaySlot extends EasySlot {

    public EasyDisplaySlot(IItemHandlerModifiable itemHandler,int index,int x,int y) { super(itemHandler,index,x,y); }
    @Override
    public boolean mayPlace(ItemStack stack) { return false; }
    @Override
    public boolean mayPickup(Player playerIn) { return false; }

}
