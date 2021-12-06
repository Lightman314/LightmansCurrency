package io.github.lightman314.lightmanscurrency.containers.inventories;

import java.util.Set;

import com.google.common.base.Supplier;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SuppliedInventory implements Container{

	public final Supplier<Container> source;
	
	public SuppliedInventory(Supplier<Container> source)
	{
		this.source = source;
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
	public void setChanged() { source.get().setChanged(); }
	
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
