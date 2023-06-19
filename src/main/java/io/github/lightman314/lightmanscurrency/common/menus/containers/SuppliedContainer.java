package io.github.lightman314.lightmanscurrency.common.menus.containers;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SuppliedContainer implements Container{

	public final Supplier<Container> source;
	private final Container nullContainer;

	@Nullable
	private List<ContainerListener> listeners;


	public SuppliedContainer(Supplier<Container> source) { this(source, new SimpleContainer(1)); }
	public SuppliedContainer(Supplier<Container> source, Container nullContainer) { this.source = source; this.nullContainer = nullContainer; }

	public void addListener(ContainerListener p_19165_) {
		if (this.listeners == null) {
			this.listeners = Lists.newArrayList();
		}

		this.listeners.add(p_19165_);
	}

	public void removeListener(ContainerListener p_19182_) {
		if (this.listeners != null) {
			this.listeners.remove(p_19182_);
		}
	}

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

	@Override
	public ItemStack getItem(int p_18941_) { return this.safeGet().getItem(p_18941_); }

	@Override
	public ItemStack removeItem(int p_18942_, int p_18943_) { return this.safeGet().removeItem(p_18942_, p_18943_); }

	@Override
	public ItemStack removeItemNoUpdate(int p_18951_) { return this.safeGet().removeItemNoUpdate(p_18951_); }

	@Override
	public void setItem(int p_18944_, ItemStack p_18945_) { this.safeGet().setItem(p_18944_, p_18945_); }

	@Override
	public int getMaxStackSize() { return this.safeGet().getMaxStackSize(); }

	@Override
	public void setChanged() {
		this.safeGet().setChanged();
		if (this.listeners != null) {
			for(ContainerListener containerlistener : this.listeners) {
				containerlistener.containerChanged(this);
			}
		}
	}

	@Override
	public boolean stillValid(Player p_18946_) { return this.safeGet().stillValid(p_18946_); }

	@Override
	public void startOpen(Player p_18955_) { this.safeGet().startOpen(p_18955_); }

	@Override
	public void stopOpen(Player p_18954_) { this.safeGet().stopOpen(p_18954_); }

	@Override
	public boolean canPlaceItem(int p_18952_, ItemStack p_18953_) { return this.safeGet().canPlaceItem(p_18952_, p_18953_); }

	@Override
	public int countItem(Item p_18948_) { return this.safeGet().countItem(p_18948_); }

	@Override
	public boolean hasAnyOf(Set<Item> p_18950_) { return this.safeGet().hasAnyOf(p_18950_); }

}