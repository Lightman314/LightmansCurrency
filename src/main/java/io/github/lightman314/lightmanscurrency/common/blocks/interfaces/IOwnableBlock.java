package io.github.lightman314.lightmanscurrency.common.blocks.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface IOwnableBlock {

	boolean canBreak(PlayerEntity player, IWorld level, BlockPos pos, BlockState state);
	
}
