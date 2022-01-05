package io.github.lightman314.lightmanscurrency.tileentity;

import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemInterfaceTileEntity extends TileEntity{
	
	
	public ItemInterfaceTileEntity() {
		super(ModTileEntities.ITEM_INTERFACE);
	}
	
	//Item capability for hopper and item automation
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			Block block = this.getBlockState().getBlock();
			if(block instanceof IItemHandlerBlock)
			{
				IItemHandlerBlock handlerBlock = (IItemHandlerBlock)block;
				IItemHandlerTileEntity tileEntity = handlerBlock.getItemHandlerEntity(this.getBlockState(), this.world, this.pos);
				if(tileEntity != null)
				{
					IItemHandler handler = tileEntity.getItemHandler(handlerBlock.getRelativeSide(this.getBlockState(), side));
					if(handler != null)
						return LazyOptional.of(() -> handler).cast();
				}
			}
		}
		return super.getCapability(cap, side);
	}
	
	public interface IItemHandlerBlock
	{
		public Direction getRelativeSide(BlockState state, Direction side);
		public IItemHandlerTileEntity getItemHandlerEntity(BlockState state, World world, BlockPos pos);
		
		public static Direction getRelativeSide(Direction facing, Direction side)
		{
			if(side.getAxis() == Axis.Y)
				return side;
			return Direction.byHorizontalIndex(facing.getHorizontalIndex() + side.getHorizontalIndex());
		}
	}
	
	public interface IItemHandlerTileEntity
	{
		public IItemHandler getItemHandler(Direction relativeSide);
	}
	
	
}
