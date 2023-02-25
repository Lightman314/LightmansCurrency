package io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public interface IRotatableBlock {

	/**
	 * Gets the BlockPos of the block to the given blocks right.
	 * @param pos This blocks BlockPos.
	 * @param facing This blocks rotational direction.
	 */
	static BlockPos getRightPos(BlockPos pos, Direction facing)
	{
		return switch (facing) {
			case NORTH -> new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
			case SOUTH -> new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
			case EAST -> new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
			case WEST -> new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
			default -> pos;
		};
	}

	/**
	 * Gets the BlockPos of the block to the given blocks left.
	 * @param pos This blocks BlockPos.
	 * @param facing This blocks rotational direction.
	 */
	static BlockPos getLeftPos(BlockPos pos, Direction facing) {
		return switch (facing) {
			case NORTH -> new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
			case SOUTH -> new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
			case EAST -> new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
			case WEST -> new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
			default -> pos;
		};
	}
	
	/**
	 * Gets the BlockPos of the block behind the given block.
	 * @param pos This blocks BlockPos.
	 * @param facing This blocks rotational direction.
	 */
	static BlockPos getForwardPos(BlockPos pos, Direction facing) {
		return switch (facing) {
			case NORTH -> new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
			case SOUTH -> new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
			case EAST -> new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
			case WEST -> new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
			default -> pos;
		};
	}
	
	/**
	 * Gets the BlockPos of the block in front of the given block.
	 * @param pos This blocks BlockPos.
	 * @param facing This blocks rotational direction.
	 */
	static BlockPos getBackwardPos(BlockPos pos, Direction facing) {
		return switch (facing) {
			case NORTH -> new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
			case SOUTH -> new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
			case EAST -> new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
			case WEST -> new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
			default -> pos;
		};
	}

	/**
	 * Gets the local right direction based on the blocks rotation.
	 * @param facing The rotatable blocks facing.
	 */
	static Vector3f getRightVect(Direction facing) {
		return switch (facing) {
			case NORTH -> new Vector3f(1f, 0f, 0f);
			case SOUTH -> new Vector3f(-1f, 0f, 0f);
			case EAST -> new Vector3f(0f, 0f, 1f);
			case WEST -> new Vector3f(0f, 0f, -1f);
			default -> new Vector3f(0f, 0f, 0f);
		};
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
		return switch (facing) {
			case NORTH -> new Vector3f(0f, 0f, -1f);
			case SOUTH -> new Vector3f(0f, 0f, 1f);
			case EAST -> new Vector3f(1f, 0f, 0f);
			case WEST -> new Vector3f(-1f, 0f, 0f);
			default -> new Vector3f(0f, 0f, 0f);
		};
	}
	
	/**
	 * Gets the local backward (toward the front) direction based on the blocks rotation.
	 * @param facing The rotatable blocks facing.
	 */
	static Vector3f getBackwardVect(Direction facing) {
		return MathUtil.VectorMult(getForwardVect(facing), -1f);
	}
	
	/**
	 * Gets the Vector3f offset from the world-defined bottom-left corner, to the local bottom-left of the block.
	 * @param facing The rotatable blocks facing.
	 */
	static Vector3f getOffsetVect(Direction facing)
	{
		return switch (facing) {
			case NORTH -> new Vector3f(0f, 0f, 1f);
			case SOUTH -> new Vector3f(1f, 0f, 0f);
			case WEST -> new Vector3f(1f, 0f, 1f);
			default -> new Vector3f(0f, 0f, 0f);
		};
	}
	
	/**
	 * Gets the rotational direction of the given rotatable block state.
	 */
	Direction getFacing(BlockState state);
	
	static Direction getRelativeSide(Direction facing, Direction side)
	{
		if(side == null)
			return null;
		if(side.getAxis() == Axis.Y)
			return side;
		//Since my facings are backwards, invert it
		if(facing.getAxis() == Axis.Z)
			facing = facing.getOpposite();
		return Direction.from2DDataValue(facing.get2DDataValue() + side.get2DDataValue());
	}
	
	static Direction getActualSide(Direction facing, Direction relativeSide)
	{
		if(relativeSide == null)
			return null;
		if(relativeSide.getAxis() == Axis.Y)
			return relativeSide;
		//Since my facings are backwards, invert it
		if(facing.getAxis() == Axis.Z)
			facing = facing.getOpposite();
		Direction result = Direction.from2DDataValue(facing.get2DDataValue() - relativeSide.get2DDataValue() + 4);
		return result.getAxis() == Axis.X ? result.getOpposite() : result;
	}
	
}
