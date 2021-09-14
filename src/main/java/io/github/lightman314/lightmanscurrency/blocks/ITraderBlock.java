package io.github.lightman314.lightmanscurrency.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface ITraderBlock {

	public TileEntity getTileEntity(BlockState state, IWorld world, BlockPos pos);
	
}
