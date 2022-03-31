package io.github.lightman314.lightmanscurrency.menus.slots;

import javax.annotation.Nonnull;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DisplaySlot extends SimpleSlot {

	public DisplaySlot(Container inventory, int index, int x, int y) { super(inventory, index, x, y); }
	
	@Override
	public boolean mayPlace(ItemStack item) { return false; }
	
	@Override
	public boolean mayPickup(Player player) { return false; }
	
	@Override
	public void set(@Nonnull ItemStack stack) { }
	
}
