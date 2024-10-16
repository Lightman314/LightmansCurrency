package io.github.lightman314.lightmanscurrency.api.misc.blocks;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public interface IMultiBlock {

    default boolean isReplaceable(@Nonnull Level level, @Nonnull BlockPos pos)
    {
        if(level.getBlockState(pos).canBeReplaced())
        {
            LightmansCurrency.LogDebug("Block at " + pos.toShortString() + " is air, and can be replaced.");
            return true;
        }
        return false;
    }

}