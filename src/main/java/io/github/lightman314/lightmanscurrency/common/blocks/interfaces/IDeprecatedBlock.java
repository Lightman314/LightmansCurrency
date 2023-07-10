package io.github.lightman314.lightmanscurrency.common.blocks.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public interface IDeprecatedBlock {

    Block replacementBlock();
    default boolean acceptableReplacementState(@Nonnull BlockState state) { return this.replacementBlock() == state.getBlock(); }
    void replaceBlock(Level level, BlockPos pos, BlockState oldState);

}
