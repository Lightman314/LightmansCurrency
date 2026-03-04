package io.github.lightman314.lightmanscurrency.api.misc.menus.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class EasyItemHandlerDisplaySlot extends EasyItemHandlerSlot {

    public EasyItemHandlerDisplaySlot(IItemHandlerModifiable itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }
    @Override
    public boolean mayPlace(ItemStack stack) { return false; }
    @Override
    public boolean mayPickup(Player playerIn) { return false; }

}
