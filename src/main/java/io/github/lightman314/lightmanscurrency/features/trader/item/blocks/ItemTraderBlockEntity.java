package io.github.lightman314.lightmanscurrency.features.trader.item.blocks;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderArguments;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.NetworkNode;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.core.LCBlockEntities;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCTraderTypes;
import io.github.lightman314.lightmanscurrency.features.trader.item.nodes.ItemTradesNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

public class ItemTraderBlockEntity extends TraderBlockEntity.SingleType {

    public ItemTraderBlockEntity(BlockPos worldPosition, BlockState blockState) { this(LCBlockEntities.ITEM_TRADER.get(),worldPosition,blockState); }
    public ItemTraderBlockEntity(BlockEntityType<?> type,BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    @Override
    protected void addAdditionalArguments(TraderArguments arguments,@Nullable Player player) {
        if(this.getBlockState().getBlock() instanceof ItemTraderBlock b)
        {
            arguments.with(NetworkNode.TYPE,b.defaultNetworkVisibility())
                    .with(ItemTradesNode.TYPE,b.defaultTradeCount());
        }
    }

    @Override
    protected TraderType getTraderType() { return LCTraderTypes.ITEM_TRADER.get(); }
}
