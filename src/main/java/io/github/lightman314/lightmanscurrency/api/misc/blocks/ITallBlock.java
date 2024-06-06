package io.github.lightman314.lightmanscurrency.api.misc.blocks;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nonnull;

public interface ITallBlock {

	BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;

	default BlockPos getOtherHeight(BlockPos pos, BlockState state) {
		if(this.getIsBottom(state))
			return pos.above();
		return pos.below();
	}
	
	default boolean getIsBottom(BlockState state) { return state.getValue(ISBOTTOM); }
	
	default boolean getIsTop(BlockState state) { return !this.getIsBottom(state); }

	default boolean isReplaceable(@Nonnull Level level, @Nonnull BlockPos pos)
	{
		if(level.getBlockState(pos).getBlock() == Blocks.AIR)
		{
			LightmansCurrency.LogDebug("Block at " + pos.toShortString() + " is air, and can be replaced.");
			return true;
		}
		return false;
	}

}
