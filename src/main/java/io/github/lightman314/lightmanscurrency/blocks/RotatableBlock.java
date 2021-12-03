package io.github.lightman314.lightmanscurrency.blocks;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
//import net.minecraft.util.Mirror;
//import net.minecraft.state.Property;
//import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;

public class RotatableBlock extends Block implements IRotatableBlock{
	
	private final VoxelShape SHAPE;
	
	public RotatableBlock(Properties properties)
	{
		super(properties);
		SHAPE = makeCuboidShape(0d,0d,0d,16d,16d,16d);
	}
	
	public RotatableBlock(Properties properties, VoxelShape customShape)
	{
		super(properties);
		SHAPE = customShape;
	}
	
	
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return super.getStateForPlacement(context).with(FACING, context.getPlacementHorizontalFacing());
	}
	
	@Override
	public BlockState rotate(BlockState state, Rotation rotation)
	{
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}
	
	@Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext contect)
	{
		return SHAPE;
	}

	@Override
	public BlockPos getRightPos(BlockPos pos, Direction facing) {
		switch (facing)
		{
			case NORTH:
				return new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
			case SOUTH:
				return new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
			case EAST:
				return new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
			case WEST:
				return new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
			default:
				return pos;
		}
	}

	@Override
	public BlockPos getLeftPos(BlockPos pos, Direction facing) {
		switch (facing)
		{
			case NORTH:
				return new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
			case SOUTH:
				return new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
			case EAST:
				return new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
			case WEST:
				return new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
			default:
				return pos;
		}
	}

	@Override
	public BlockPos getForwardPos(BlockPos pos, Direction facing) {
		switch (facing)
		{
			case NORTH:
				return new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
			case SOUTH:
				return new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
			case EAST:
				return new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
			case WEST:
				return new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
			default:
				return pos;
		}
	}

	@Override
	public BlockPos getBackwardPos(BlockPos pos, Direction facing) {
		switch (facing)
		{
			case NORTH:
				return new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
			case SOUTH:
				return new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
			case EAST:
				return new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
			case WEST:
				return new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
			default:
				return pos;
		}
	}

	@Override
	public Vector3f getRightVect(Direction facing) {
		switch (facing)
		{
			case NORTH:
				return new Vector3f(1f, 0f, 0f);
			case SOUTH:
				return new Vector3f(-1f, 0f, 0f);
			case EAST:
				return new Vector3f(0f, 0f, 1f);
			case WEST:
				return new Vector3f(0f, 0f, -1f);
			default:
				return new Vector3f(0f,0f,0f);
		}
	}

	@Override
	public Vector3f getLeftVect(Direction facing) {
		return MathUtil.VectorMult(getRightVect(facing), -1f);
	}

	@Override
	public Vector3f getForwardVect(Direction facing) {
		switch (facing)
		{
			case NORTH:
				return new Vector3f(0f, 0f, -1f);
			case SOUTH:
				return new Vector3f(0f, 0f, 1f);
			case EAST:
				return new Vector3f(1f, 0f, 0f);
			case WEST:
				return new Vector3f(-1f, 0f, 0f);
			default:
				return new Vector3f(0f,0f,0f);
		}
	}
	
	public Vector3f getOffsetVect(Direction facing)
	{
		switch (facing)
		{
			case NORTH:
				return new Vector3f(0f, 0f, 1f);
			case SOUTH:
				return new Vector3f(1f, 0f, 0f);
			//case EAST:
			//	return new Vector3f(0f, 0f, 0f);
			case WEST:
				return new Vector3f(1f, 0f, 1f);
			default:
				return new Vector3f(0f,0f,0f);
		}
	}

	@Override
	public Vector3f getBackwardVect(Direction facing) {
		return MathUtil.VectorMult(getForwardVect(facing), -1f);
	}
	
	@Override
	public Direction getFacing(BlockState state)
	{
		return state.get(FACING);
	}
	
}
