package io.github.lightman314.lightmanscurrency.api.world.block.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface ITallBlock extends IMultiBlock {

    BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;

    default BlockPos getOtherHeight(BlockPos pos, BlockState state)
    {
        if(this.isBottomBlock(state))
            return pos.above();
        return pos.below();
    }
    default BlockPos getBottomBlock(BlockPos pos,BlockState state) { return this.isBottomBlock(state) ? pos : pos.below(); }
    default BlockPos getTopBlock(BlockPos pos,BlockState state) { return this.isBottomBlock(state) ? pos.above() : pos; }

    default boolean isBottomBlock(BlockState state) { return state.getValue(ISBOTTOM); }
    default boolean isTopBlock(BlockState state) { return !this.isBottomBlock(state); }

}