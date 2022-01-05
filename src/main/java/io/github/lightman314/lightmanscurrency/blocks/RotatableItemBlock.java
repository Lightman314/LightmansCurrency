package io.github.lightman314.lightmanscurrency.blocks;

import io.github.lightman314.lightmanscurrency.tileentity.ItemInterfaceTileEntity.IItemHandlerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;

public abstract class RotatableItemBlock extends RotatableBlock implements IItemHandlerBlock{
	
	public RotatableItemBlock(Properties properties)
	{
		super(properties);
	}
	
	public RotatableItemBlock(Properties properties, VoxelShape customShape)
	{
		super(properties, customShape);
	}
	
	@Override
	public Direction getRelativeSide(BlockState state, Direction side)
	{
		Direction facing = state.get(FACING);
		return IItemHandlerBlock.getRelativeSide(facing, side);
	}
	
}
