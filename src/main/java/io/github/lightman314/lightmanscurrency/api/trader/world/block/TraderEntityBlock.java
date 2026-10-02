package io.github.lightman314.lightmanscurrency.api.trader.world.block;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface TraderEntityBlock extends EntityBlock {

    default void validateAfterLoadingStoredTrader(TraderData trader, BlockState state) {}

    @Override
    @Nullable
    TraderBlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState);
}
