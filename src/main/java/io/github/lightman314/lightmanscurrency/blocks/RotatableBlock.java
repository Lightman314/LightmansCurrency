package io.github.lightman314.lightmanscurrency.blocks;

import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.math.Vector3f;

public class RotatableBlock extends Block implements IRotatableBlock{
	
	private final VoxelShape SHAPE;
	
	public RotatableBlock(Properties properties)
	{
		super(properties);
		SHAPE = LazyShapes.BOX;
	}
	
	public RotatableBlock(Properties properties, VoxelShape customShape)
	{
		super(properties);
		SHAPE = customShape;
	}
	
	
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection());
	}
	
	@Override
	public BlockState rotate(BlockState state, Rotation rotation)
	{
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}
	
	@Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
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
		return state.getValue(FACING);
	}
	
}
