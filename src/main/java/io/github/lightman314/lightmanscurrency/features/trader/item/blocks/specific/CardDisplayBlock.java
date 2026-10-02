package io.github.lightman314.lightmanscurrency.features.trader.item.blocks.specific;

import io.github.lightman314.lightmanscurrency.api.trader.world.block.RotatableTraderBlock;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlock;

public class CardDisplayBlock extends RotatableTraderBlock implements ItemTraderBlock {

    public CardDisplayBlock(Properties properties) { super(properties); }

    @Override
    public int defaultTradeCount() { return 4; }

}
