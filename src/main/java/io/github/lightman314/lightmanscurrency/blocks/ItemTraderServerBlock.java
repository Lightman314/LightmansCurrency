package io.github.lightman314.lightmanscurrency.blocks;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.ITraderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ItemTraderServerBlock extends RotatableBlock implements ITraderBlock, EntityBlock{

	final int tradeCount;
	
	public ItemTraderServerBlock(Properties properties, int tradeCount)
	{
		super(properties);
		this.tradeCount = tradeCount;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new UniversalItemTraderBlockEntity(pos, state, this.tradeCount);
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!level.isClientSide)
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof UniversalTraderBlockEntity && player instanceof Player)
			{
				UniversalTraderBlockEntity trader = (UniversalTraderBlockEntity)blockEntity;
				if(stack.hasCustomHoverName())
					trader.init((Player)player, stack.getHoverName().getString());
				else
					trader.init((Player)player);
			}
		}
	}
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof UniversalTraderBlockEntity)
		{
			UniversalTraderBlockEntity trader = (UniversalTraderBlockEntity)blockEntity;
			if(!trader.canBreak(player))
				return;
			trader.onDestroyed();
		}
		
		super.playerWillDestroy(level, pos, state, player);
		
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof UniversalTraderBlockEntity)
			{
				UniversalTraderBlockEntity trader = (UniversalTraderBlockEntity)blockEntity;
				//Update the owner
				if(trader.hasPermissions(player))
				{
					//CurrencyMod.LOGGER.info("Updating the owner name.");
					trader.updateOwner(player);
					trader.openStorageMenu(player);
				}
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockEntity getBlockEntity(BlockState state, LevelAccessor level, BlockPos pos) {
		return level.getBlockEntity(pos);
	}
	
}
