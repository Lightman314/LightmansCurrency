package io.github.lightman314.lightmanscurrency.features.trader.item.blocks.specific;

public class DoubleShelfBlock extends SingleShelfBlock {

    public DoubleShelfBlock(Properties properties) { super(properties); }

    @Override
    public int defaultTradeCount() { return 4; }

}
