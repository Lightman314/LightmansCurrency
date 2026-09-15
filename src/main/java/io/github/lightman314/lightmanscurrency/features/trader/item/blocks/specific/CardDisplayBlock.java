package io.github.lightman314.lightmanscurrency.features.trader.item.blocks.specific;

import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.WoodType;
import io.github.lightman314.lightmanscurrency.api.trader.world.block.RotatableTraderBlock;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IColoredBlock;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IWoodenBlock;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlock;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CardDisplayBlock extends RotatableTraderBlock implements ItemTraderBlock, IColoredBlock, IWoodenBlock {

    private final WoodType wood;
    private final VanillaColor color;
    public CardDisplayBlock(Properties properties, WoodType wood,VanillaColor color) {
        super(properties);
        this.wood = wood;
        this.color = color;
    }

    @Override
    public WoodType getWoodType() {
        return this.wood;
    }

    @Override
    public VanillaColor getColor() { return this.color; }

    @Override
    public int defaultTradeCount() { return 4; }

    @Override
    @Nullable
    public TraderBlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) { return new ItemTraderBlockEntity(worldPosition,blockState); }

}
