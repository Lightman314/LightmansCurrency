package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.common.blockentity.interfaces.ICapabilityBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

public class CapabilityInterfaceBlockEntity extends TileEntity {
	
	
	public CapabilityInterfaceBlockEntity() {
		super(ModBlockEntities.CAPABILITY_INTERFACE.get());
	}
	
	//Item capability for hopper and item automation
	@Override
	public <T> @Nonnull LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side)
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof ICapabilityBlock)
		{
			ICapabilityBlock handlerBlock = (ICapabilityBlock)block;
			TileEntity blockEntity = handlerBlock.getCapabilityBlockEntity(this.getBlockState(), this.level, this.worldPosition);
			if(blockEntity != null)
				return blockEntity.getCapability(cap, side);
		}
		return super.getCapability(cap, side);
	}
	
	
}