package io.github.lightman314.lightmanscurrency.api.upgrades;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface IUpgradeableBlock {

    @Nullable
    default IUpgradeable getUpgradeable(Level level, BlockPos pos, BlockState state) {
        if(level.getBlockEntity(pos) instanceof IUpgradeableBlockEntity be)
            return be.getUpgradeable();
        return null;
    }

    default boolean canUseUpgradeItem(IUpgradeable upgradeable, ItemStack stack, @Nullable Player player) { return true; }

}
