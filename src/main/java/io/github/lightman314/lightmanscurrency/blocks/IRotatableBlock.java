package io.github.lightman314.lightmanscurrency.blocks;

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
	public BlockPos getRightPos(BlockPos pos, Direction facing);
	/**
	 * Gets the BlockPos of the block to the given blocks left.
	 * @param pos This blocks BlockPos.
	 * @param facing This blocks rotational direction.
	 */
	public BlockPos getLeftPos(BlockPos pos, Direction facing);
	/**
	 * Gets the BlockPos of the block behind the given block.
	 * @param pos This blocks BlockPos.
	 * @param facing This blocks rotational direction.
	 */
	public BlockPos getForwardPos(BlockPos pos, Direction facing);
	/**
	 * Gets the BlockPos of the block in front of the given block.
	 * @param pos This blocks BlockPos.
	 * @param facing This blocks rotational direction.
	 */
	public BlockPos getBackwardPos(BlockPos pos, Direction facing);
	
	/**
	 * Gets the local right direction based on the blocks rotation.
	 * @param facing The rotatable blocks facing.
	 */
	public Vector3f getRightVect(Direction facing);
	/**
	 * Gets the local left direction based on the blocks rotation.
	 * @param facing The rotatable blocks facing.
	 */
	public Vector3f getLeftVect(Direction facing);
	/**
	 * Gets the local forward (toward the back) direction based on the blocks rotation.
	 * @param facing The rotatable blocks facing.
	 */
	public Vector3f getForwardVect(Direction facing);
	/**
	 * Gets the local backward (toward the front) direction based on the blocks rotation.
	 * @param facing The rotatable blocks facing.
	 */
	public Vector3f getBackwardVect(Direction facing);
	/**
	 * Gets the Vector3f offset from the world-defined bottom-left corner, to the local bottom-left of the block.
	 * @param facing The rotatable blocks facing.
	 */
	public Vector3f getOffsetVect(Direction facing);
	
	/**
	 * Gets the rotational direction of the given rotatable block state.
	 */
	public Direction getFacing(BlockState state);
}
