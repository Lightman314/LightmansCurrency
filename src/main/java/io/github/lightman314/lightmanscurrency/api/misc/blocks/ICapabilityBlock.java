package io.github.lightman314.lightmanscurrency.api.misc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface ICapabilityBlock
{
	default BlockPos getCapabilityBlockPos(BlockState state, BlockPos pos) {
		if(this instanceof ITallBlock tallBlock && tallBlock.getIsTop(state))
			pos = pos.below();
		if(this instanceof IWideBlock wideBlock && wideBlock.getIsRight(state))
			pos = wideBlock.getOtherSide(pos,state);
		if(this instanceof IDeepBlock deepBlock && deepBlock.getIsBack(state))
			pos = deepBlock.getOtherDepth(pos,state);
		return pos;
	}

}