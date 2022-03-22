package io.github.lightman314.lightmanscurrency.menus.containers;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SuppliedContainer implements Container{

	public final Supplier<Container> source;
	
	@Nullable
	private List<ContainerListener> listeners;
	
	public SuppliedContainer(Supplier<Container> source)
	{
		this.source = source;
	}
	
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
	
	@Override
	public void clearContent() { source.get().clearContent(); }
	
	@Override
	public int getContainerSize() { return source.get().getContainerSize(); }
	
	@Override
	public boolean isEmpty() { return source.get().isEmpty(); }
	
	@Override
	public ItemStack getItem(int p_18941_) { return source.get().getItem(p_18941_); }
	
	@Override
	public ItemStack removeItem(int p_18942_, int p_18943_) { return source.get().removeItem(p_18942_, p_18943_); }
	
	@Override
	public ItemStack removeItemNoUpdate(int p_18951_) { return source.get().removeItemNoUpdate(p_18951_); }
	
	@Override
	public void setItem(int p_18944_, ItemStack p_18945_) { source.get().setItem(p_18944_, p_18945_); }
	
	@Override
	public int getMaxStackSize() { return source.get().getMaxStackSize(); }
	
	@Override
	public void setChanged() {
		source.get().setChanged();
		if (this.listeners != null) {
			for(ContainerListener containerlistener : this.listeners) {
				containerlistener.containerChanged(this);
			}
		}
	}
	
	@Override
	public boolean stillValid(Player p_18946_) { return source.get().stillValid(p_18946_); }
	
	@Override
	public void startOpen(Player p_18955_) { source.get().startOpen(p_18955_); }
	
	@Override
	public void stopOpen(Player p_18954_) { source.get().stopOpen(p_18954_); }
	
	@Override
	public boolean canPlaceItem(int p_18952_, ItemStack p_18953_) { return source.get().canPlaceItem(p_18952_, p_18953_); }
	
	@Override
	public int countItem(Item p_18948_) { return source.get().countItem(p_18948_); }
	
	@Override
	public boolean hasAnyOf(Set<Item> p_18950_) { return source.get().hasAnyOf(p_18950_); }
	
}
