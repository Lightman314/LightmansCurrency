package io.github.lightman314.lightmanscurrency.common.menus;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MenuUtil {

    public static boolean stillValid(Player player, BlockEntity blockEntity)
    {
        if(player == null || blockEntity == null)
            return false;
        if(blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) != blockEntity)
            return false;
        return stillValid(player, blockEntity.getBlockPos());
    }

    public static boolean stillValid(Player player, BlockPos blockPos)
    {
        return !(player.distanceToSqr((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D) > 64.0D);
    }

}
