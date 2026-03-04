package io.github.lightman314.lightmanscurrency.api.misc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IMultiBlock {

    default boolean isReplaceable(Level level, BlockPos pos)
    {
        return level.getBlockState(pos).canBeReplaced();
    }

}
