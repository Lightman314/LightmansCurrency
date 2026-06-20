package io.github.lightman314.lightmanscurrency.features.coins;


import io.github.lightman314.lightmanscurrency.api.world.block.EasyShapes;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IRotatableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;

public class FallingCoinPile extends FallingCoinBlock implements IRotatableBlock, SimpleWaterloggedBlock {

    public FallingCoinPile(Properties properties) { super(properties); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.WATERLOGGED)
                        .add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(pos);
        return super.getStateForPlacement(context).setValue(FACING,context.getHorizontalDirection().getOpposite()).setValue(BlockStateProperties.WATERLOGGED,fluidState.is(Fluids.WATER));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) { return super.rotate(state, rotation).setValue(FACING,rotation.rotate(this.getFacing(state))); }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) { return Shapes.empty(); }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return EasyShapes.SLAB; }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if(state.getValue(BlockStateProperties.WATERLOGGED))
            ticks.scheduleTick(pos,Fluids.WATER,Fluids.WATER.getTickDelay(level));
        return super.updateShape(state,level,ticks,pos,directionToNeighbour,neighbourPos,neighbourState,random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) { return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state); }

}
