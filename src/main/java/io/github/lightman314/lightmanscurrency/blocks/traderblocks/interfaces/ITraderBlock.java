package io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces;

import io.github.lightman314.lightmanscurrency.blockentity.interfaces.IOwnableBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.interfaces.IOwnableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface ITraderBlock extends IOwnableBlock{

	public BlockEntity getTileEntity(BlockState state, LevelAccessor level, BlockPos pos);
	
	default boolean canBreak(Player player, LevelAccessor level, BlockPos pos, BlockState state)
	{
		BlockEntity blockEntity = this.getTileEntity(state, level, pos);
		if(blockEntity instanceof IOwnableBlockEntity)
		{
			IOwnableBlockEntity ownableBlockEntity = (IOwnableBlockEntity)blockEntity;
			return ownableBlockEntity.canBreak(player);
		}
		return true;
	}
	
}
