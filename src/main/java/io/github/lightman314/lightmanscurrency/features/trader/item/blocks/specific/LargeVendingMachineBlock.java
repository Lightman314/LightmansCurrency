package io.github.lightman314.lightmanscurrency.features.trader.item.blocks.specific;

import io.github.lightman314.lightmanscurrency.api.trader.world.block.TallWideRotatableTraderBlock;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlock;

public class LargeVendingMachineBlock extends TallWideRotatableTraderBlock implements ItemTraderBlock {

    public LargeVendingMachineBlock(Properties properties) { super(properties); }

    @Override
    public int defaultTradeCount() { return 12; }

}
