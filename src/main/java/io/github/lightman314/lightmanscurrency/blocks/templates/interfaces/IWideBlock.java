package io.github.lightman314.lightmanscurrency.blocks.templates.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface IWideBlock {
	
	default BlockPos getOtherSide(BlockPos pos, BlockState state, Direction facing) {
		if(this.getIsLeft(state))
			return IRotatableBlock.getRightPos(pos, facing);
		return IRotatableBlock.getLeftPos(pos, facing);
	}
	
	public boolean getIsLeft(BlockState state);
	
}
