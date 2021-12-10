package io.github.lightman314.lightmanscurrency.blocks.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface IOwnableBlock {

	public boolean canBreak(Player player, LevelAccessor level, BlockPos pos, BlockState state);
	
}
