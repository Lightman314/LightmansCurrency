package io.github.lightman314.lightmanscurrency.blocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ITraderBlock {

	public BlockEntity getTileEntity(BlockState state, LevelAccessor level, BlockPos pos);
	
}
