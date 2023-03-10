package io.github.lightman314.lightmanscurrency.integration.immersiveengineering;

import blusunrize.immersiveengineering.common.util.RotationUtil;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.ITallBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IWideBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LCImmersive {

    public static void registerRotationBlacklists() {
        try{ RotationUtil.blacklist.add(LCImmersive::allowRotation); } catch (Throwable ignored) {}
    }

    private static boolean allowRotation(World level, BlockPos blockPos) {
        BlockState state = level.getBlockState(blockPos);
        return !(state.getBlock() instanceof ITallBlock) && !(state.getBlock() instanceof IWideBlock);
    }

}
