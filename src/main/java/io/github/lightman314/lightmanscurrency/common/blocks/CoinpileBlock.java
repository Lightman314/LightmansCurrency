package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public class CoinpileBlock extends CoinBlock implements IRotatableBlock, IWaterLoggable {
	
	private final VoxelShape shape;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	
	public CoinpileBlock(Properties properties, Supplier<IItemProvider> coinItem)
	{
		this(properties, coinItem, LazyShapes.SHORT_BOX_T);
	}
	
	public CoinpileBlock(Properties properties, Supplier<IItemProvider> coinItem, VoxelShape shape)
	{
		super(properties, coinItem);
		this.shape = shape != null ? shape : LazyShapes.SHORT_BOX_T;
		this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
	}
	
	@Override
	protected int getCoinCount() { return 9; }
	
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockPos blockpos = context.getClickedPos();
		FluidState fluidstate = context.getLevel().getFluidState(blockpos);
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection()).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
	}
	
	@Nonnull
	@Override
	public BlockState rotate(BlockState state, Rotation rotation)
	{
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}
	
	@Override
    protected void createBlockStateDefinition(@Nonnull StateContainer.Builder<Block, BlockState> builder)
    {
		super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(WATERLOGGED);
    }
	
	@Nonnull
	public BlockState updateShape(BlockState p_56381_, @Nonnull Direction p_56382_, @Nonnull BlockState p_56383_, @Nonnull IWorld p_56384_, @Nonnull BlockPos p_56385_, @Nonnull BlockPos p_56386_) {
		if (p_56381_.getValue(WATERLOGGED)) {
			p_56384_.getLiquidTicks().scheduleTick(p_56385_, Fluids.WATER, Fluids.WATER.getTickDelay(p_56384_));
		}

		return super.updateShape(p_56381_, p_56382_, p_56383_, p_56384_, p_56385_, p_56386_);
	}
	
	@Nonnull
	@Override
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull ISelectionContext contect)
	{
		return shape;
	}
	
	@Override
	public Direction getFacing(BlockState state)
	{
		return state.getValue(FACING);
	}
	
	@Nonnull
	@SuppressWarnings("deprecation")
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}
	
	@Override
	public boolean isPathfindable(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull PathType type) {
		if (type == PathType.WATER) {
			return worldIn.getFluidState(pos).is(FluidTags.WATER);
		}
		return false;
	}
	
}
