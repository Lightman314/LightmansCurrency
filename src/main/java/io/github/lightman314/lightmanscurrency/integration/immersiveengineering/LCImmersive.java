package io.github.lightman314.lightmanscurrency.integration.immersiveengineering;

import blusunrize.immersiveengineering.common.util.orientation.RotationUtil;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.ITallBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IWideBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class LCImmersive {

    public static void registerRotationBlacklists() {
        RotationUtil.blacklist.add(LCImmersive::allowRotation);
    }

    private static boolean allowRotation(Level level, BlockPos blockPos) {
        BlockState state = level.getBlockState(blockPos);
        return !(state.getBlock() instanceof ITallBlock) && !(state.getBlock() instanceof IWideBlock);
    }

}
