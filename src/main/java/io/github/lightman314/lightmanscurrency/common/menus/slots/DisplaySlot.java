package io.github.lightman314.lightmanscurrency.common.menus.slots;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class DisplaySlot extends SimpleSlot {

	public DisplaySlot(IInventory inventory, int index, int x, int y) { super(inventory, index, x, y); }
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack item) { return false; }
	
	@Override
	public boolean mayPickup(@Nonnull PlayerEntity player) { return false; }
	
	@Override
	public void set(@Nonnull ItemStack stack) { }
	
}
