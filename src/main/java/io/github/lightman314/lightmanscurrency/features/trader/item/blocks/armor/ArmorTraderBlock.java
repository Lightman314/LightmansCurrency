package io.github.lightman314.lightmanscurrency.features.trader.item.blocks.armor;

import io.github.lightman314.lightmanscurrency.api.trader.world.block.TallRotatableTraderBlock;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class ArmorTraderBlock extends TallRotatableTraderBlock implements ItemTraderBlock {

    public ArmorTraderBlock(Properties properties) { super(properties); }
    public ArmorTraderBlock(Properties properties, VoxelShape shape) { super(properties, shape); }
    public ArmorTraderBlock(Properties properties, BiFunction<Direction, Boolean, VoxelShape> shape) { super(properties, shape); }

    @Override
    public int defaultTradeCount() { return 4; }

    @Override
    @Nullable
    public TraderBlockEntity createBlockEntity(BlockPos worldPosition, BlockState blockState) { return new ArmorDisplayTraderBlockEntity(worldPosition,blockState); }

}