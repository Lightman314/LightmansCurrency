package io.github.lightman314.lightmanscurrency.api.misc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IWideBlock extends IMultiBlock, IRotatableBlock {

	BooleanProperty ISLEFT = BlockStateProperties.ATTACHED;

	default BlockPos getOtherSide(BlockPos pos, BlockState state) {
		if(this.getIsLeft(state))
			return IRotatableBlock.getRightPos(pos, this.getFacing(state));
		return IRotatableBlock.getLeftPos(pos, this.getFacing(state));
	}
	
	default boolean getIsLeft(BlockState state) { return state.getValue(ISLEFT); }
	
	default boolean getIsRight(BlockState state) { return !getIsLeft(state); }
	
}
