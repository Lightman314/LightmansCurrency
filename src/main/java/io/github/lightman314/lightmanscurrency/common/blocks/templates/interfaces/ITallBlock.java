package io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface ITallBlock {

	default BlockPos getOtherHeight(BlockPos pos, BlockState state) {
		if(this.getIsBottom(state))
			return pos.above();
		return pos.below();
	}
	
	public boolean getIsBottom(BlockState state);
	
	default boolean getIsTop(BlockState state) { return !this.getIsBottom(state); }
	
}
