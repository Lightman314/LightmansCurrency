package io.github.lightman314.lightmanscurrency.api.world.block.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IMultiBlock {

    default BlockPos getBlockEntityPosition(BlockPos pos,BlockState state)
    {
        if(this instanceof ITallBlock tallBlock)
            pos = tallBlock.getBottomBlock(pos,state);
        if(this instanceof IWideBlock wideBlock)
            pos = wideBlock.getLeftBlock(pos,state);
        if(this instanceof IDeepBlock deepBlock)
            pos = deepBlock.getFrontBlock(pos,state);
        return pos;
    }

    default boolean isReplaceable(Level level,BlockPos pos) { return level.getBlockState(pos).canBeReplaced(); }
    default boolean isReplaceable(Level level, BlockPos pos, BlockPlaceContext context) { return level.getBlockState(pos).canBeReplaced(context); }

}