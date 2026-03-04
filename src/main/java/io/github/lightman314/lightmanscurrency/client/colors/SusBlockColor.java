package io.github.lightman314.lightmanscurrency.client.colors;

import io.github.lightman314.lightmanscurrency.common.blockentity.CoinJarBlockEntity;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SusBlockColor implements BlockColor {

    public static final SusBlockColor INSTANCE = new SusBlockColor();

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int layer) {
        if(layer == 0 && level != null && pos != null && level.getBlockEntity(pos) instanceof CoinJarBlockEntity jarBlock)
            return jarBlock.getColor();
        return 0xFFFFFF;
    }

}
