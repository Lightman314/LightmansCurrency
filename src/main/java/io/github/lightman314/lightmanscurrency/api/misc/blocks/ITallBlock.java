package io.github.lightman314.lightmanscurrency.api.misc.blocks;

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
	
	default boolean getIsBottom(BlockState state) { return state.getValue(ISBOTTOM); }
	
	default boolean getIsTop(BlockState state) { return !this.getIsBottom(state); }
	
}
