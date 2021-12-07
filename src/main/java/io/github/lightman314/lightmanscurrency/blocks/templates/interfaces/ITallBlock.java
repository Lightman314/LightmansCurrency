package io.github.lightman314.lightmanscurrency.blocks.templates.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface ITallBlock {

	default BlockPos getOtherHeight(BlockPos pos, BlockState state) {
		if(this.getIsBottom(state))
			return pos.above();
		return pos.below();
	}
	
	public boolean getIsBottom(BlockState state);
	
	default boolean getIsTop(BlockState state) { return !this.getIsBottom(state); }
	
}
