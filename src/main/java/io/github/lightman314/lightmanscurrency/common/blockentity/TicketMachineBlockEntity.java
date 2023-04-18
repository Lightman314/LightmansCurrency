package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;

public class TicketMachineBlockEntity extends EasyBlockEntity {

	Inventory storage = new Inventory(2);
	public IInventory getStorage() { return this.storage; }
	
	public TicketMachineBlockEntity()
	{
		super(ModBlockEntities.TICKET_MACHINE.get());
		this.storage.addListener(c -> this.setChanged());
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundNBT compound)
	{
		InventoryUtil.saveAllItems("Items", compound, this.storage);
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundNBT compound)
	{
		this.storage = InventoryUtil.loadAllItems("Items", compound, 2);
		this.storage.addListener(c -> this.setChanged());
	}
	
}
