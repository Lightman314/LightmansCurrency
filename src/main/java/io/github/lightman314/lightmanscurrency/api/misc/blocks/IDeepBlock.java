package io.github.lightman314.lightmanscurrency.api.misc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IDeepBlock extends IMultiBlock, IRotatableBlock{

    BooleanProperty IS_FRONT = BooleanProperty.create("front");

    default BlockPos getOtherDepth(BlockPos pos, BlockState state) {
        if(this.getIsFront(state))
            return IRotatableBlock.getForwardPos(pos, this.getFacing(state));
        return IRotatableBlock.getBackwardPos(pos, this.getFacing(state));
    }

    default boolean getIsFront(BlockState state) { return state.getValue(IS_FRONT); }

    default boolean getIsBack(BlockState state) { return !this.getIsFront(state); }

}