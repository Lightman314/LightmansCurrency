package io.github.lightman314.lightmanscurrency.containers.inventories;

import com.google.common.base.Supplier;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SuppliedInventory implements Container{

	public final Supplier<Container> source;
	
	public SuppliedInventory(Supplier<Container> source)
	{
		this.source = source;
	}

	@Override
	public void clearContent() {
		source.get().clearContent();
	}

	@Override
	public int getContainerSize() {
		return source.get().getContainerSize();
	}

	@Override
	public boolean isEmpty() {
		return source.get().isEmpty();
	}

	@Override
	public ItemStack getItem(int index) {
		return source.get().getItem(index);
	}

	@Override
	public ItemStack removeItem(int index, int amount) {
		return source.get().removeItem(index, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return source.get().removeItemNoUpdate(index);
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		source.get().setItem(index, stack);
	}

	@Override
	public void setChanged() {
		source.get().setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return source.get().stillValid(player);
	}
	
}
