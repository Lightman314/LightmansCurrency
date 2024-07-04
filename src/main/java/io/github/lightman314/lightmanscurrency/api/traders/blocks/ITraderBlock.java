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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITraderBlock extends IOwnableBlock, ICapabilityBlock {

	@Nullable
	BlockEntity getBlockEntity(@Nonnull BlockState state, @Nonnull LevelAccessor level, @Nonnull BlockPos pos);
	
	default boolean canBreak(@Nonnull Player player, @Nonnull LevelAccessor level, @Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof IOwnableBlockEntity ownableBlockEntity)
			return ownableBlockEntity.canBreak(player);
		return true;
	}

	default ItemStack getDropBlockItem(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) { return new ItemStack(state.getBlock()); }

	@Override
	@Nonnull
	default BlockPos getCapabilityBlockPos(BlockState state, Level level, BlockPos pos) {
		BlockEntity be = this.getBlockEntity(state,level,pos);
		if(be != null)
			return be.getBlockPos();
		return pos;
	}
	
}
