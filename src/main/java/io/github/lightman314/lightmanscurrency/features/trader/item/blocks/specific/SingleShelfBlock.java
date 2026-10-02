package io.github.lightman314.lightmanscurrency.features.trader.item.blocks.specific;

import io.github.lightman314.lightmanscurrency.api.trader.world.block.RotatableTraderBlock;
import io.github.lightman314.lightmanscurrency.api.world.block.ShapeHelper;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public class SingleShelfBlock extends RotatableTraderBlock implements ItemTraderBlock {

    private static final VoxelShape SHAPE_NORTH = box(0d,0d,11d,16d,16d,16d);
    private static final VoxelShape SHAPE_SOUTH =  box(0d,0d,0d,16d,16d,5d);
    private static final VoxelShape SHAPE_EAST = box(0d,0d,0d,5d,16d,16d);
    private static final VoxelShape SHAPE_WEST = box(11d,0d,0d,16d,16d,16d);
    public static final Function<Direction,VoxelShape> SHAPES = ShapeHelper.directionalShape(SHAPE_NORTH,SHAPE_EAST,SHAPE_SOUTH,SHAPE_WEST);

    public SingleShelfBlock(Properties properties) { super(properties,SHAPES); }

    @Override
    public int defaultTradeCount() { return 1; }

}
