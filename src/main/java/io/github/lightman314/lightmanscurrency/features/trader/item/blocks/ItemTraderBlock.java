package io.github.lightman314.lightmanscurrency.features.trader.item.blocks;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.world.block.TraderEntityBlock;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.AbstractItemTradesNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface ItemTraderBlock extends TraderEntityBlock {

    MultiLineTextEntry TOOLTIP = MultiLineTextEntry.tooltip(LCApi.MODID,"trader.item");
    MultiLineTextEntry ARMOR_TOOLTIP = MultiLineTextEntry.tooltip(LCApi.MODID,"trader.item.armor");

    int defaultTradeCount();

    default boolean defaultNetworkVisibility() { return false; }

    @Override
    default void validateAfterLoadingStoredTrader(TraderData trader, BlockState state) {
        //Update the item trades node to have *at minimum* the expected trade count of this trader
        AbstractItemTradesNode<?> tradesNode = trader.getFirstNode(AbstractItemTradesNode.class);
        if(tradesNode != null)
            tradesNode.safeUpdateBaseCount(this.defaultTradeCount());
    }

    @Override
    @Nullable
    default TraderBlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) { return new ItemTraderBlockEntity(worldPosition,blockState); }

}
