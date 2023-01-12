package io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces;

import io.github.lightman314.lightmanscurrency.blockentity.interfaces.ICapabilityBlock;
import io.github.lightman314.lightmanscurrency.blockentity.interfaces.IOwnableBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.interfaces.IOwnableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface ITraderBlock extends IOwnableBlock, ICapabilityBlock {

	BlockEntity getBlockEntity(BlockState state, LevelAccessor level, BlockPos pos);
	
	default boolean canBreak(Player player, LevelAccessor level, BlockPos pos, BlockState state)
	{
		BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof IOwnableBlockEntity ownableBlockEntity)
			return ownableBlockEntity.canBreak(player);
		return true;
	}
	
	default ItemStack getDropBlockItem(Level level, BlockPos pos, BlockState state) { return state != null ? new ItemStack(state.getBlock()): ItemStack.EMPTY; }
	
	default BlockEntity getCapabilityBlockEntity(BlockState state, Level level, BlockPos pos) { return this.getBlockEntity(state, level, pos); }
	
}
