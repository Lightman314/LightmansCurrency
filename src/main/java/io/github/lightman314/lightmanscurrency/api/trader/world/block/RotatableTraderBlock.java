package io.github.lightman314.lightmanscurrency.api.trader.world.block;

import io.github.lightman314.lightmanscurrency.api.world.block.ShapeHelper;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IRotatableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.function.Function;

public abstract class RotatableTraderBlock extends TraderBlock implements IRotatableBlock {

    private final Function<Direction, VoxelShape> shape;

    public RotatableTraderBlock(Properties properties) { this(properties, ShapeHelper.BOX); }
    public RotatableTraderBlock(Properties properties,VoxelShape shape) { this(properties, ShapeHelper.singleShape(shape)); }
    public RotatableTraderBlock(Properties properties,Function<Direction,VoxelShape> shape) {
        super(properties);
        this.shape = shape;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected BlockState initializeDefaultState(BlockState state) {
        return super.initializeDefaultState(state).setValue(FACING,Direction.NORTH);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) { return super.getStateForPlacement(context).setValue(FACING,context.getHorizontalDirection().getOpposite()); }
    @Override
    public BlockState rotate(BlockState state, Rotation direction) { return state.setValue(FACING,direction.rotate(state.getValue(FACING))); }

    @Override
    protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        return this.shape.apply(state.getValue(FACING));
    }

}