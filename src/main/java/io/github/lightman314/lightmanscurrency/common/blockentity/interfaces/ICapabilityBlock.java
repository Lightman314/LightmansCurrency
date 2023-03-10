package io.github.lightman314.lightmanscurrency.common.blockentity.interfaces;


import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICapabilityBlock
{
	TileEntity getCapabilityBlockEntity(BlockState state, World level, BlockPos pos);
}