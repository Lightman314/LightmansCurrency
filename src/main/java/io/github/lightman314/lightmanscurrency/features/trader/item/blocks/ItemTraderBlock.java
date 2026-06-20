package io.github.lightman314.lightmanscurrency.features.trader.item.blocks;

import io.github.lightman314.lightmanscurrency.api.trader.world.block.TraderBlock;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public abstract class ItemTraderBlock extends TraderBlock {

    public ItemTraderBlock(Properties properties) { super(properties); }

    public abstract int defaultTradeCount();

    public boolean defaultNetworkVisibility() { return false; }

    @Override
    @Nullable
    public TraderBlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) { return new ItemTraderBlockEntity(worldPosition,blockState); }

}
