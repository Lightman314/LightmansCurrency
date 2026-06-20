package io.github.lightman314.lightmanscurrency.features.trader.item.blocks.specific;

import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IColoredBlock;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlock;
import net.minecraft.world.item.DyeColor;

public class DisplayCaseBlock extends ItemTraderBlock implements IColoredBlock {

    private final DyeColor color;
    public DisplayCaseBlock(Properties properties,DyeColor color) { super(properties); this.color = color; }

    @Override
    public int defaultTradeCount() { return 1; }

    @Override
    public DyeColor getColor() { return this.color; }

}
