package io.github.lightman314.lightmanscurrency.features.trader.item.blocks.armor;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.core.LCBlockEntities;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCTraderTypes;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ArmorDisplayTraderBlockEntity extends ItemTraderBlockEntity {

    public ArmorDisplayTraderBlockEntity(BlockPos worldPosition, BlockState blockState) { this(LCBlockEntities.ARMOR_TRADER.get(),worldPosition, blockState); }
    protected ArmorDisplayTraderBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) { super(type,worldPosition,blockState); }

    @Override
    protected TraderType getTraderType() { return LCTraderTypes.ARMOR_DISPLAY.get(); }

}