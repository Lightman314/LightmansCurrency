package io.github.lightman314.lightmanscurrency.menus.slots;

import javax.annotation.Nonnull;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DisplaySlot extends SimpleSlot {

	public DisplaySlot(Container inventory, int index, int x, int y) { super(inventory, index, x, y); }
	
	@Override
	public boolean mayPlace(@NotNull ItemStack item) { return false; }
	
	@Override
	public boolean mayPickup(@NotNull Player player) { return false; }
	
	@Override
	public void set(@Nonnull ItemStack stack) { }
	
}
