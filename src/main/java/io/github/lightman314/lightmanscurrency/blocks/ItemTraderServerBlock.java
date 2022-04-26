package io.github.lightman314.lightmanscurrency.blocks;

import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
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
				if(trader.hasPermission(player, Permissions.OPEN_STORAGE))
				{
					//CurrencyMod.LOGGER.info("Updating the owner name.");
					trader.updateNames(player);
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
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCTooltips.ITEM_NETWORK_TRADER);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
}
