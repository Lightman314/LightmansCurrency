package io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface ITraderBlock {

	public BlockEntity getTileEntity(BlockState state, LevelAccessor world, BlockPos pos);
	
}
