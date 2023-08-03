package io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IWideBlock {

	BooleanProperty ISLEFT = BlockStateProperties.ATTACHED;

	default BlockPos getOtherSide(BlockPos pos, BlockState state, Direction facing) {
		if(this.getIsLeft(state))
			return IRotatableBlock.getRightPos(pos, facing);
		return IRotatableBlock.getLeftPos(pos, facing);
	}
	
	boolean getIsLeft(BlockState state);
	
	default boolean getIsRight(BlockState state) { return !getIsLeft(state); }
	
}
