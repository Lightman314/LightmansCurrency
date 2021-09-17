package io.github.lightman314.lightmanscurrency.tileentity;

import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

public class TicketMachineTileEntity extends TileEntity{

	IInventory storage = new Inventory(2);
	public IInventory getStorage() { return this.storage; }
	
	public TicketMachineTileEntity()
	{
		super(ModTileEntities.TICKET_MACHINE);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		
		InventoryUtil.saveAllItems("Items", compound, this.storage);
		
		return super.write(compound);
	}
	
	public void read(BlockState state, CompoundNBT compound)
	{
		
		this.storage = InventoryUtil.loadAllItems("Items", compound, 2);
		super.read(state, compound);
		
	}
	
}
