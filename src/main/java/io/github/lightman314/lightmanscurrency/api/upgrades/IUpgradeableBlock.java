package io.github.lightman314.lightmanscurrency.api.upgrades;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IUpgradeableBlock {

    @Nullable
    default IUpgradeable getUpgradeable(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        if(level.getBlockEntity(pos) instanceof IUpgradeableBlockEntity be)
            return be.getUpgradeable();
        return null;
    }

    default boolean canUseUpgradeItem(@Nonnull IUpgradeable upgradeable, @Nonnull ItemStack stack, @Nullable Player player) { return true; }

}
