package io.github.lightman314.lightmanscurrency.api.misc.menus.slots;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class OutputSlot extends EasySlot {

    public OutputSlot(IItemHandlerModifiable itemHandler,int index,int x,int y) { super(itemHandler,index,x,y); }
    @Override
    public boolean mayPlace(ItemStack stack) { return false; }

}
