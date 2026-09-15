package io.github.lightman314.lightmanscurrency.features.trader.item.blocks;

import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface ItemTraderBlock extends EntityBlock {

    int defaultTradeCount();

    default boolean defaultNetworkVisibility() { return false; }

    @Override
    @Nullable
    default TraderBlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) { return new ItemTraderBlockEntity(worldPosition,blockState); }

}
