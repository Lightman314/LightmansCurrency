package io.github.lightman314.lightmanscurrency.api.misc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface ICapabilityBlock
{
	BlockEntity getCapabilityBlockEntity(BlockState state, Level level, BlockPos pos);
}