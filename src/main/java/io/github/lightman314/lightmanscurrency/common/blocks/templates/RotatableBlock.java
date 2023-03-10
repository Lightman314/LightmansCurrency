package io.github.lightman314.lightmanscurrency.common.blocks.templates;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;

public class RotatableBlock extends Block implements IRotatableBlock{
	
	private final Function<Direction, VoxelShape> shape;
	
	public RotatableBlock(Properties properties)
	{
		this(properties, LazyShapes.BOX_SHAPE_T);
	}
	
	public RotatableBlock(Properties properties, VoxelShape shape) {
		this(properties, LazyShapes.lazySingleShape(shape));
	}
	
	public RotatableBlock(Properties properties, Function<Direction,VoxelShape> shape)
	{
		super(properties);
		this.shape = shape;
	}
	
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	
	@Override
	public BlockState getStateForPlacement(@Nonnull BlockItemUseContext context)
	{
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection());
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
    }
	
	@Nonnull
	@Override
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader level, @Nonnull BlockPos pos, @Nonnull ISelectionContext context)
	{
		return shape.apply(this.getFacing(state));
	}
	
	@Override
	public Direction getFacing(BlockState state)
	{
		return state.getValue(FACING);
	}
	
}
