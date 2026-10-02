package io.github.lightman314.lightmanscurrency.api.world.block.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface IProtectedBlock {

    boolean canBreakBlock(LevelAccessor level, BlockState state, BlockPos pos, Player player);

}
