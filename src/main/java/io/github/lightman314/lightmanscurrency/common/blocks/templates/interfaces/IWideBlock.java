package io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public interface IWideBlock {
	
	default BlockPos getOtherSide(BlockPos pos, BlockState state, Direction facing) {
		if(this.getIsLeft(state))
			return IRotatableBlock.getRightPos(pos, facing);
		return IRotatableBlock.getLeftPos(pos, facing);
	}
	
	boolean getIsLeft(BlockState state);
	
	default boolean getIsRight(BlockState state) { return !getIsLeft(state); }
	
}
