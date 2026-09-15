package io.github.lightman314.lightmanscurrency.features.trader.item.blocks.specific;

import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.api.trader.world.block.TraderBlock;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IColoredBlock;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlock;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class DisplayCaseBlock extends TraderBlock implements ItemTraderBlock, IColoredBlock {

    private final VanillaColor color;
    public DisplayCaseBlock(Properties properties, VanillaColor color) { super(properties); this.color = color; }

    @Override
    public int defaultTradeCount() { return 1; }

    @Override
    public VanillaColor getColor() { return this.color; }

    @Override
    @Nullable
    public TraderBlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) { return new ItemTraderBlockEntity(worldPosition,blockState); }

}
