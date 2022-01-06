package io.github.lightman314.lightmanscurrency.blockentity;

import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemInterfaceBlockEntity extends BlockEntity{
	
	
	public ItemInterfaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ITEM_INTERFACE, pos, state);
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
				IItemHandlerBlockEntity blockEntity = handlerBlock.getItemHandlerEntity(this.getBlockState(), this.level, this.worldPosition);
				if(blockEntity != null)
				{
					IItemHandler handler = blockEntity.getItemHandler(handlerBlock.getRelativeSide(this.getBlockState(), side));
					if(handler != null)
						return LazyOptional.of(() -> handler).cast();
					else
						return LazyOptional.empty();
				}
			}
		}
		return super.getCapability(cap, side);
	}
	
	public interface IItemHandlerBlock
	{
		public Direction getRelativeSide(BlockState state, Direction side);
		public IItemHandlerBlockEntity getItemHandlerEntity(BlockState state, Level level, BlockPos pos);
		
		public static Direction getRelativeSide(Direction facing, Direction side)
		{
			if(side.getAxis() == Axis.Y)
				return side;
			//Since my facings are backwards, invert it
			if(facing.getAxis() == Axis.Z)
				facing = facing.getOpposite();
			return Direction.from2DDataValue(facing.get2DDataValue() + side.get2DDataValue());
		}
	}
	
	public interface IItemHandlerBlockEntity
	{
		public IItemHandler getItemHandler(Direction relativeSide);
	}
	
	
}
