package io.github.lightman314.lightmanscurrency.api.traders.blocks;

import io.github.lightman314.lightmanscurrency.api.misc.blocks.ICapabilityBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.IOwnableBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IOwnableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface ITraderBlock extends IOwnableBlock, ICapabilityBlock {

	@Nullable
	default BlockEntity getBlockEntity(BlockState state, LevelAccessor level, BlockPos pos)
	{
		return level.getBlockEntity(this.getCapabilityBlockPos(state,level,pos));
	}
	
	default boolean canBreak(Player player, LevelAccessor level, BlockPos pos, BlockState state)
	{
		BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof IOwnableBlockEntity ownableBlockEntity)
			return ownableBlockEntity.canBreak(player);
		return true;
	}

	default ItemStack getDropBlockItem(Level level, BlockPos pos, BlockState state) { return new ItemStack(state.getBlock()); }
	
}
