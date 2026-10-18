package io.github.lightman314.lightmanscurrency.api.trader.world.block;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IDeepBlock;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IMultiBlock;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.ITallBlock;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IWideBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

public interface TraderEntityBlock extends EntityBlock {

    default void validateAfterLoadingStoredTrader(TraderData trader, BlockState state) {}

    @Nullable
    default TraderBlockEntity getBlockEntity(LevelAccessor level, BlockState state, BlockPos pos)
    {
        if(this instanceof IMultiBlock mb)
            pos = mb.getBlockEntityPosition(pos,state);
        if(level.getBlockEntity(pos) instanceof TraderBlockEntity be)
            return be;
        return null;
    }

    default boolean isBlockEntityState(BlockState state) {
        return switch (this) {
            case ITallBlock tb when tb.isTopBlock(state) -> false;
            case IWideBlock wb when wb.isRightBlock(state) -> false;
            case IDeepBlock db when db.isBackBlock(state) -> false;
            default -> true;
        };
    }

    @Override
    @Nullable
    @ApiStatus.NonExtendable
    default BlockEntity newBlockEntity(BlockPos worldPosition,BlockState blockState) {
        if(this.isBlockEntityState(blockState))
            return this.createBlockEntity(worldPosition,blockState);
        return null;
    }

    @Nullable
    TraderBlockEntity createBlockEntity(BlockPos worldPosition, BlockState blockState);
}
