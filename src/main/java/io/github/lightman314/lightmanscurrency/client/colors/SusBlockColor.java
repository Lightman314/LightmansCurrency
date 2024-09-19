package io.github.lightman314.lightmanscurrency.client.colors;

import io.github.lightman314.lightmanscurrency.common.blockentity.CoinJarBlockEntity;
import io.github.lightman314.lightmanscurrency.common.items.CoinJarItem;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SusBlockColor implements BlockColor, ItemColor {

    public static final SusBlockColor INSTANCE = new SusBlockColor();

    @Override
    public int getColor(@Nonnull BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int layer) {
        if(layer == 0 && level.getBlockEntity(pos) instanceof CoinJarBlockEntity jarBlock)
            return jarBlock.getColor();
        return 0xFFFFFF;
    }

    @Override
    public int getColor(@Nonnull ItemStack stack, int layer) {
        if(layer == 0)
            return CoinJarItem.getJarColor(stack);
        return 0xFFFFFFFF;
    }

}
