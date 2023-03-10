package io.github.lightman314.lightmanscurrency.common.menus.containers;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SuppliedContainer implements IInventory {

	public final Supplier<IInventory> source;
	
	@Nullable
	private List<IInventoryChangedListener> listeners;
	
	public SuppliedContainer(Supplier<IInventory> source)
	{
		this.source = source;
	}
	
	public void addListener(IInventoryChangedListener l) {
		if (this.listeners == null) {
			this.listeners = Lists.newArrayList();
		}

		this.listeners.add(l);
	}

	public void removeListener(IInventoryChangedListener l) {
		if (this.listeners != null) {
			this.listeners.remove(l);
     	}
	}
	
	@Override
	public void clearContent() { source.get().clearContent(); }
	
	@Override
	public int getContainerSize() { return source.get().getContainerSize(); }
	
	@Override
	public boolean isEmpty() { return source.get().isEmpty(); }
	
	@Nonnull
	@Override
	public ItemStack getItem(int p_18941_) { return source.get().getItem(p_18941_); }
	
	@Nonnull
	@Override
	public ItemStack removeItem(int p_18942_, int p_18943_) { return source.get().removeItem(p_18942_, p_18943_); }
	
	@Nonnull
	@Override
	public ItemStack removeItemNoUpdate(int p_18951_) { return source.get().removeItemNoUpdate(p_18951_); }
	
	@Override
	public void setItem(int p_18944_, @Nonnull ItemStack p_18945_) { source.get().setItem(p_18944_, p_18945_); }
	
	@Override
	public int getMaxStackSize() { return source.get().getMaxStackSize(); }
	
	@Override
	public void setChanged() {
		source.get().setChanged();
		if (this.listeners != null) {
			for(IInventoryChangedListener containerlistener : this.listeners) {
				containerlistener.containerChanged(this);
			}
		}
	}
	
	@Override
	public boolean stillValid(@Nonnull PlayerEntity p_18946_) { return source.get().stillValid(p_18946_); }
	
	@Override
	public void startOpen(@Nonnull PlayerEntity p_18955_) { source.get().startOpen(p_18955_); }
	
	@Override
	public void stopOpen(@Nonnull PlayerEntity p_18954_) { source.get().stopOpen(p_18954_); }
	
	@Override
	public boolean canPlaceItem(int p_18952_, @Nonnull ItemStack p_18953_) { return source.get().canPlaceItem(p_18952_, p_18953_); }
	
	@Override
	public int countItem(@Nonnull Item p_18948_) { return source.get().countItem(p_18948_); }
	
	@Override
	public boolean hasAnyOf(@Nonnull Set<Item> p_18950_) { return source.get().hasAnyOf(p_18950_); }
	
}
