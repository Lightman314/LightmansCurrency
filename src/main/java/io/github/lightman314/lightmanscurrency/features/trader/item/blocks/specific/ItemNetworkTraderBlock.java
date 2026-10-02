package io.github.lightman314.lightmanscurrency.features.trader.item.blocks.specific;

import io.github.lightman314.lightmanscurrency.api.trader.world.block.RotatableTraderBlock;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ItemNetworkTraderBlock extends RotatableTraderBlock implements ItemTraderBlock {

    private final int tier;
    public ItemNetworkTraderBlock(Properties properties,int tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public int defaultTradeCount() { return this.tier * 4; }

    @Override
    public boolean defaultNetworkVisibility() { return true; }

    @Nullable
    protected Block getNextTier() { return LCBlocks.ITEM_NETWORK_TRADER.get(this.tier + 1); }

    @Override
    protected boolean shouldChangedStateKeepBlockEntity(BlockState oldState) { return oldState.getBlock() instanceof ItemNetworkTraderBlock; }
}
