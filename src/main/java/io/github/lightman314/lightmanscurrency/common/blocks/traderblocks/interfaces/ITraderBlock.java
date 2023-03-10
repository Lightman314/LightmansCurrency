package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces;

import io.github.lightman314.lightmanscurrency.common.blockentity.interfaces.ICapabilityBlock;
import io.github.lightman314.lightmanscurrency.common.blockentity.interfaces.IOwnableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IOwnableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public interface ITraderBlock extends IOwnableBlock, ICapabilityBlock {

	 TileEntity getBlockEntity(BlockState state, IWorld level, BlockPos pos);
	
	default boolean canBreak(PlayerEntity player, IWorld level, BlockPos pos, BlockState state)
	{
		TileEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof IOwnableBlockEntity)
		{
			IOwnableBlockEntity ownableBlockEntity = (IOwnableBlockEntity)blockEntity;
			return ownableBlockEntity.canBreak(player);
		}
		return true;
	}
	
	default ItemStack getDropBlockItem(World level, BlockPos pos, BlockState state) { return state != null ? new ItemStack(state.getBlock()): ItemStack.EMPTY; }
	
	default TileEntity getCapabilityBlockEntity(BlockState state, World level, BlockPos pos) { return this.getBlockEntity(state, level, pos); }
	
}