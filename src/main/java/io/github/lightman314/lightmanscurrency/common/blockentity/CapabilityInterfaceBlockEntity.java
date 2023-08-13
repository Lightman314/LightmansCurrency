package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.common.blockentity.interfaces.ICapabilityBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class CapabilityInterfaceBlockEntity extends BlockEntity{
	
	public CapabilityInterfaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CAPABILITY_INTERFACE.get(), pos, state);
	}
	
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side)
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof ICapabilityBlock handlerBlock)
		{
			BlockEntity blockEntity = handlerBlock.getCapabilityBlockEntity(this.getBlockState(), this.level, this.worldPosition);
			if(blockEntity != null && !(blockEntity instanceof CapabilityInterfaceBlockEntity))
				return blockEntity.getCapability(cap, side);
		}
		return super.getCapability(cap, side);
	}
	
	
}
