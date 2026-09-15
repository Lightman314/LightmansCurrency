package io.github.lightman314.lightmanscurrency.api.world.menu.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class VanillaDisplaySlot extends EasyVanillaSlot {

    public VanillaDisplaySlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }

    @Override
    public boolean mayPlace(ItemStack itemStack) { return false; }
    @Override
    public boolean mayPickup(Player player) { return false; }
    @Override
    public ItemStack remove(int amount) { return ItemStack.EMPTY; }

}
