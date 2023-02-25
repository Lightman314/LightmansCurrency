package io.github.lightman314.lightmanscurrency.common.blocks.interfaces;

import io.github.lightman314.lightmanscurrency.common.blockentity.TickableBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface IEasyEntityBlock extends EntityBlock {

    Collection<BlockEntityType<?>> getAllowedTypes();

    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if(this.getAllowedTypes().contains(type))
            return TickableBlockEntity.createTicker(level, state, type);
        return null;
    }

}
