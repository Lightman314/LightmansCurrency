package io.github.lightman314.lightmanscurrency.common.menus.slots;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.common.menus.slots.easy.EasySlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DisplaySlot extends EasySlot {

	public DisplaySlot(Container inventory, int index, int x, int y) { super(inventory, index, x, y); }
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack item) { return false; }
	
	@Override
	public boolean mayPickup(@Nonnull Player player) { return false; }
	
	@Override
	public void set(@Nonnull ItemStack stack) { }
	
}
