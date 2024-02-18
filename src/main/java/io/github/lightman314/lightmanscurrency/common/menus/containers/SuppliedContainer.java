package io.github.lightman314.lightmanscurrency.common.menus.containers;

import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SuppliedContainer implements Container{

	public final Supplier<Container> source;
	private final Container nullContainer;

	
	public SuppliedContainer(Supplier<Container> source) { this(source, new SimpleContainer(1)); }
	public SuppliedContainer(Supplier<Container> source, Container nullContainer) { this.source = source; this.nullContainer = nullContainer; }

	private Container safeGet() {
		Container c = this.source.get();
		return c == null ? this.nullContainer : c;
	}

	@Override
	public void clearContent() { this.safeGet().clearContent(); }
	
	@Override
	public int getContainerSize() { return this.safeGet().getContainerSize(); }
	
	@Override
	public boolean isEmpty() { return this.safeGet().isEmpty(); }
	
	@Nonnull
	@Override
	public ItemStack getItem(int slot) { return this.safeGet().getItem(slot); }
	
	@Nonnull
	@Override
	public ItemStack removeItem(int slot, int count) { return this.safeGet().removeItem(slot, count); }
	
	@Nonnull
	@Override
	public ItemStack removeItemNoUpdate(int slot) { return this.safeGet().removeItemNoUpdate(slot); }
	
	@Override
	public void setItem(int slot, @Nonnull ItemStack stack) { this.safeGet().setItem(slot, stack); }
	
	@Override
	public int getMaxStackSize() { return this.safeGet().getMaxStackSize(); }
	
	@Override
	public void setChanged() { this.safeGet().setChanged(); }
	
	@Override
	public boolean stillValid(@Nonnull Player player) { return this.safeGet().stillValid(player); }
	
	@Override
	public void startOpen(@Nonnull Player player) { this.safeGet().startOpen(player); }
	
	@Override
	public void stopOpen(@Nonnull Player player) { this.safeGet().stopOpen(player); }
	
	@Override
	public boolean canPlaceItem(int slot, @Nonnull ItemStack stack) { return this.safeGet().canPlaceItem(slot, stack); }
	
	@Override
	public int countItem(@Nonnull Item item) { return this.safeGet().countItem(item); }
	
	@Override
	public boolean hasAnyOf(@Nonnull Set<Item> items) { return this.safeGet().hasAnyOf(items); }
	
}
