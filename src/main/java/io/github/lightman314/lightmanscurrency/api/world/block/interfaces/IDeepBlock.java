package io.github.lightman314.lightmanscurrency.api.world.block.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IDeepBlock extends IMultiBlock, IRotatableBlock {

    BooleanProperty ISFRONT = BooleanProperty.create("front");

    default BlockPos getOtherDepth(BlockPos pos, BlockState state)
    {
        if(this.isFrontBlock(state))
            return IRotatableBlock.getFrontPosition(pos,this.getFacing(state));
        return IRotatableBlock.getBackPosition(pos,this.getFacing(state));
    }
    default BlockPos getFrontBlock(BlockPos pos,BlockState state) { return this.isFrontBlock(state) ? pos : IRotatableBlock.getFrontPosition(pos,this.getFacing(state)); }
    default BlockPos getBackBlock(BlockPos pos,BlockState state) { return this.isFrontBlock(state) ? IRotatableBlock.getBackPosition(pos,this.getFacing(state)) : pos; }

    default boolean isFrontBlock(BlockState state) { return state.getValue(ISFRONT); }
    default boolean isBackBlock(BlockState state) { return !this.isFrontBlock(state); }

}