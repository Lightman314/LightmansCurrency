package io.github.lightman314.lightmanscurrency.blockentity;

import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TicketMachineBlockEntity extends BlockEntity{

	Container storage = new SimpleContainer(2);
	public Container getStorage() { return this.storage; }
	
	public TicketMachineBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModTileEntities.TICKET_MACHINE, pos, state);
	}
	
	@Override
	public CompoundTag save(CompoundTag compound)
	{
		
		InventoryUtil.saveAllItems("Items", compound, this.storage);
		
		return super.save(compound);
	}
	
	public void load(CompoundTag compound)
	{
		
		this.storage = InventoryUtil.loadAllItems("Items", compound, 2);
		super.load(compound);
		
	}
	
}
