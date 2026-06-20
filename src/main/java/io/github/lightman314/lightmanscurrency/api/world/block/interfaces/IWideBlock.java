package io.github.lightman314.lightmanscurrency.api.world.block.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IWideBlock extends IMultiBlock, IRotatableBlock {

    BooleanProperty ISLEFT = BooleanProperty.create("left");

    default BlockPos getOtherSide(BlockPos pos, BlockState state) {
        if(this.isLeftBlock(state))
            return IRotatableBlock.getRightPos(pos,this.getFacing(state));
        return IRotatableBlock.getLeftPos(pos,this.getFacing(state));
    }
    default BlockPos getLeftBlock(BlockPos pos,BlockState state) { return this.isLeftBlock(state) ? pos : IRotatableBlock.getLeftPos(pos,this.getFacing(state)); }
    default BlockPos getRightBlock(BlockPos pos,BlockState state) { return this.isLeftBlock(state) ? IRotatableBlock.getRightPos(pos,this.getFacing(state)) : pos; }

    default boolean isLeftBlock(BlockState state) { return state.getValue(ISLEFT); }
    default boolean isRightBlock(BlockState state) { return !this.isLeftBlock(state); }

}
