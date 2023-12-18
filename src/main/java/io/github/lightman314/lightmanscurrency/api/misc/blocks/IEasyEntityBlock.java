package io.github.lightman314.lightmanscurrency.api.misc.blocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.TickableBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface IEasyEntityBlock extends EntityBlock {

    @Nonnull
    Collection<BlockEntityType<?>> getAllowedTypes();

    @Nonnull
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        if(this.getAllowedTypes().contains(type))
            return TickableBlockEntity.createTicker(level, state, type);
        return null;
    }

}
