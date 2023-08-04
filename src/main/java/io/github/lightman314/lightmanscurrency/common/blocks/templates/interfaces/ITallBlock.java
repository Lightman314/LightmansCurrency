package io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface ITallBlock {

	BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;

	default BlockPos getOtherHeight(BlockPos pos, BlockState state) {
		if(this.getIsBottom(state))
			return pos.above();
		return pos.below();
	}
	
	boolean getIsBottom(BlockState state);
	
	default boolean getIsTop(BlockState state) { return !this.getIsBottom(state); }
	
}
