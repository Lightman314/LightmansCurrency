package io.github.lightman314.lightmanscurrency.blocks.networktraders.templates;

import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
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

public abstract class NetworkTraderBlock extends RotatableBlock implements ITraderBlock, EntityBlock{
	
	public NetworkTraderBlock(Properties properties) { super(properties); }
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return this.createBlockEntity(pos, state); }
	
	protected abstract BlockEntity createBlockEntity(BlockPos pos, BlockState state);
	
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
			
			//Dump contents before onDestroyed, as removing the trader from the office makes it impossible to 
			trader.dumpContents(level, state, pos);
			//Now delete the trader from the trading office
			trader.onDestroyed();
			trader.flagAsRemovable();
		}
		
		super.playerWillDestroy(level, pos, state, player);
		
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean flag) {
		
		//Ignore if the block is the same.
		if(state.getBlock() == newState.getBlock())
		    return;
		
		if(!level.isClientSide)
		{
			BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
			if(blockEntity instanceof UniversalTraderBlockEntity)
			{
				UniversalTraderBlockEntity trader = (UniversalTraderBlockEntity)blockEntity;
				if(!trader.allowRemoval())
				{
					UniversalTraderData traderData = trader.getData();
					if(traderData != null)
					{
						LightmansCurrency.LogError("Trader block at " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " was broken by illegal means!");
						LightmansCurrency.LogError("Activating emergency eject protocol.");
						EjectionData data = EjectionData.create(state, traderData);
						//Eject the traders contents
						TradingOffice.handleEjectionData(level, pos, data);
						//Delete the trader
						trader.onDestroyed();
						//Flag as removable to avoid duplicate ejections
						trader.flagAsRemovable();
						//Remove the rest of the multi-block structure.
						try {
							this.onInvalidRemoval(state, level, pos, traderData);
						} catch(Throwable t) { t.printStackTrace(); }
					}
					
				}
				else
					LightmansCurrency.LogInfo("Trader block was broken by legal means!");
			}
		}
		
		super.onRemove(state, level, pos, newState, flag);
	}
	
	protected abstract void onInvalidRemoval(BlockState state, Level level, BlockPos pos, UniversalTraderData trader);
	
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
