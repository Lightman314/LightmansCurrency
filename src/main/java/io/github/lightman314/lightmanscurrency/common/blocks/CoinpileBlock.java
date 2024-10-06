package io.github.lightman314.lightmanscurrency.common.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CoinpileBlock extends CoinBlock implements IRotatableBlock, SimpleWaterloggedBlock{
	
	private final VoxelShape shape;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	
	public CoinpileBlock(Properties properties) { this(properties, LazyShapes.SHORT_BOX); }
	
	public CoinpileBlock(Properties properties, VoxelShape shape)
	{
		super(properties);
		this.shape = shape != null ? shape : LazyShapes.SHORT_BOX;
		this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	protected boolean isFullBlock() { return false; }
	
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos blockpos = context.getClickedPos();
		FluidState fluidstate = context.getLevel().getFluidState(blockpos);
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection()).setValue(WATERLOGGED, fluidstate.is(Fluids.WATER));
	}

	@Override
	@Nonnull
	@SuppressWarnings("deprecation")
	public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
	
	@Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder)
    {
		super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(WATERLOGGED);
    }
	
	@Nonnull
	public BlockState updateShape(BlockState p_56381_, @Nonnull Direction p_56382_, @Nonnull BlockState p_56383_, @Nonnull LevelAccessor p_56384_, @Nonnull BlockPos p_56385_, @Nonnull BlockPos p_56386_) {
		if (p_56381_.getValue(WATERLOGGED)) {
			p_56384_.scheduleTick(p_56385_, Fluids.WATER, Fluids.WATER.getTickDelay(p_56384_));
		}

		return super.updateShape(p_56381_, p_56382_, p_56383_, p_56384_, p_56385_, p_56386_);
	}
	
	@Override
	@Nonnull
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext contect) { return shape; }

	@Override
	@Nonnull
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public boolean isPathfindable(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull PathComputationType type) {
		if (type == PathComputationType.WATER) {
			return level.getFluidState(pos).is(FluidTags.WATER);
		}
		return false;
	}
	
}
