package io.github.lightman314.lightmanscurrency.api.misc.blocks;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.common.blocks.EasyBlock;
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

import javax.annotation.Nonnull;

public class RotatableBlock extends EasyBlock implements IRotatableBlock {
	
	private final Function<Direction,VoxelShape> shape;
	
	public RotatableBlock(Properties properties)
	{
		this(properties, LazyShapes.BOX_SHAPE);
	}
	
	public RotatableBlock(Properties properties, VoxelShape shape) {
		this(properties, LazyShapes.lazySingleShape(shape));
	}
	
	public RotatableBlock(Properties properties, Function<Direction,VoxelShape> shape)
	{
		super(properties);
		this.shape = shape;
	}
	
	@Override
	public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) { return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection()); }
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
	
	@Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) { return shape.apply(this.getFacing(state)); }
	
}
