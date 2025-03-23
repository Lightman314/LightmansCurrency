package io.github.lightman314.lightmanscurrency.api.misc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IDeepBlock {

    BooleanProperty IS_FRONT = BooleanProperty.create("front");

    default BlockPos getOtherDepth(BlockPos pos, BlockState state, Direction facing) {
        if(this.getIsFront(state))
            return IRotatableBlock.getForwardPos(pos, facing);
        return IRotatableBlock.getBackwardPos(pos, facing);
    }

    default boolean getIsFront(BlockState state) { return state.getValue(IS_FRONT); }

    default boolean getIsBack(BlockState state) { return !this.getIsFront(state); }

}
