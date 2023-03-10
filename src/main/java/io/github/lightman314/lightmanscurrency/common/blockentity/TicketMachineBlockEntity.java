package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class TicketMachineBlockEntity extends TileEntity {

	Inventory storage = new Inventory(2);
	public IInventory getStorage() { return this.storage; }
	
	public TicketMachineBlockEntity()
	{
		super(ModBlockEntities.TICKET_MACHINE.get());
		this.storage.addListener(c -> this.setChanged());
	}
	
	@Nonnull
	@Override
	public CompoundNBT save(@Nonnull CompoundNBT compound)
	{
		compound = super.save(compound);
		InventoryUtil.saveAllItems("Items", compound, this.storage);
		return compound;
	}
	
	public void load(@Nonnull BlockState state, @Nonnull CompoundNBT compound)
	{
		super.load(state, compound);
		this.storage = InventoryUtil.loadAllItems("Items", compound, 2);
		this.storage.addListener(c -> this.setChanged());
	}
	
}
