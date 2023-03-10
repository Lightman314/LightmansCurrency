package io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;

public interface IRotatableBlock {

	/**
	 * Gets the BlockPos of the block to the given blocks right.
	 * @param pos This blocks BlockPos.
	 * @param facing This blocks rotational direction.
	 */
	static BlockPos getRightPos(BlockPos pos, Direction facing)
	{
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

	/**
	 * Gets the BlockPos of the block to the given blocks left.
	 * @param pos This blocks BlockPos.
	 * @param facing This blocks rotational direction.
	 */
	static BlockPos getLeftPos(BlockPos pos, Direction facing) {
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
	
	/**
	 * Gets the BlockPos of the block behind the given block.
	 * @param pos This blocks BlockPos.
	 * @param facing This blocks rotational direction.
	 */
	static BlockPos getForwardPos(BlockPos pos, Direction facing) {
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
	
	/**
	 * Gets the BlockPos of the block in front of the given block.
	 * @param pos This blocks BlockPos.
	 * @param facing This blocks rotational direction.
	 */
	static BlockPos getBackwardPos(BlockPos pos, Direction facing) {
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

	/**
	 * Gets the local right direction based on the blocks rotation.
	 * @param facing The rotatable blocks facing.
	 */
	static Vector3f getRightVect(Direction facing) {
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
	
	/**
	 * Gets the local left direction based on the blocks rotation.
	 * @param facing The rotatable blocks facing.
	 */
	static Vector3f getLeftVect(Direction facing) {
		return MathUtil.VectorMult(getRightVect(facing), -1f);
	}

	/**
	 * Gets the local forward (toward the back) direction based on the blocks rotation.
	 * @param facing The rotatable blocks facing.
	 */
	static Vector3f getForwardVect(Direction facing) {
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
	
	/**
	 * Gets the local backward (toward the front) direction based on the blocks rotation.
	 * @param facing The rotatable blocks facing.
	 */
	public static Vector3f getBackwardVect(Direction facing) {
		return MathUtil.VectorMult(getForwardVect(facing), -1f);
	}
	
	/**
	 * Gets the Vector3f offset from the world-defined bottom-left corner, to the local bottom-left of the block.
	 * @param facing The rotatable blocks facing.
	 */
	static Vector3f getOffsetVect(Direction facing)
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
	
	/**
	 * Gets the rotational direction of the given rotatable block state.
	 */
	Direction getFacing(BlockState state);
	
	static Direction getRelativeSide(Direction facing, Direction side)
	{
		if(side == null)
			return side;
		if(side.getAxis() == Direction.Axis.Y)
			return side;
		//Since my facings are backwards, invert it
		if(facing.getAxis() == Direction.Axis.Z)
			facing = facing.getOpposite();
		return Direction.from2DDataValue(facing.get2DDataValue() + side.get2DDataValue());
	}
	
	public static Direction getActualSide(Direction facing, Direction relativeSide)
	{
		if(relativeSide == null)
			return relativeSide;
		if(relativeSide.getAxis() == Direction.Axis.Y)
			return relativeSide;
		//Since my facings are backwards, invert it
		if(facing.getAxis() == Direction.Axis.Z)
			facing = facing.getOpposite();
		Direction result = Direction.from2DDataValue(facing.get2DDataValue() - relativeSide.get2DDataValue() + 4);
		return result.getAxis() == Direction.Axis.X ? result.getOpposite() : result;
	}
	
}