package io.github.lightman314.lightmanscurrency.containers.inventories;

import com.google.common.base.Supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SuppliedInventory implements IInventory{

	public final Supplier<IInventory> source;
	
	public SuppliedInventory(Supplier<IInventory> source)
	{
		this.source = source;
	}
	
	@Override
	public void clear() {
		source.get().clear();
	}

	@Override
	public ItemStack decrStackSize(int arg0, int arg1) {
		return source.get().decrStackSize(arg0, arg1);
	}
	
	@Override
	public int getSizeInventory() {
		if(source == null || source.get() == null)
			return 0;
		return source.get().getSizeInventory();
	}
	

	@Override
	public ItemStack getStackInSlot(int arg0) {
		return source.get().getStackInSlot(arg0);
	}
	

	@Override
	public boolean isEmpty() {
		return source.get().isEmpty();
	}
	

	@Override
	public boolean isUsableByPlayer(PlayerEntity arg0) {
		return source.get().isUsableByPlayer(arg0);
	}
	

	@Override
	public void markDirty() {
		source.get().markDirty();
	}
	

	@Override
	public ItemStack removeStackFromSlot(int arg0) {
		return source.get().removeStackFromSlot(arg0);
	}

	@Override
	public void setInventorySlotContents(int arg0, ItemStack arg1) {
		source.get().setInventorySlotContents(arg0, arg1);
	}
	
}
