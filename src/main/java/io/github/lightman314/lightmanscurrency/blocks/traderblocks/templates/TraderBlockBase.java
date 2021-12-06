package io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.blockentity.DummyBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TickableBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.ITraderBlock;
import io.github.lightman314.lightmanscurrency.blocks.util.TickerUtil;
import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class TraderBlockBase extends Block implements ITraderBlock, EntityBlock {

	public TraderBlockBase(Properties properties)
	{
		super(properties);
	}
	
	protected boolean shouldMakeTrader(BlockState state) { return true; }
	protected abstract BlockEntity makeTrader(BlockPos pos, BlockState state);
	
	@Nullable 
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return TickerUtil.createTickerHelper(type, ModTileEntities.ITEM_TRADER, TickableBlockEntity::tickHandler);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		if(this.shouldMakeTrader(state))
			return this.makeTrader(pos, state);
		return new DummyBlockEntity(pos, state);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			BlockEntity blockEntity = this.getTileEntity(state, level, pos);
			if(blockEntity instanceof TraderBlockEntity)
			{
				TraderBlockEntity trader = (TraderBlockEntity)blockEntity;
				//Update the owner's name
				if(trader.isOwner(player))
					trader.setOwner(player);
				//Send update packet for safety, and open the menu
				TileEntityUtil.sendUpdatePacket(blockEntity);
				trader.openTradeMenu(player);
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		this.setPlacedByBase(level, pos, state, player, stack);
	}
	
	public void setPlacedByBase(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!level.isClientSide)
		{
			BlockEntity blockEntity = this.getTileEntity(state, level, pos);
			if(blockEntity instanceof TraderBlockEntity)
			{
				TraderBlockEntity trader = (TraderBlockEntity)blockEntity;
				trader.setOwner(player);
				if(stack.hasCustomHoverName())
					trader.setCustomName(stack.getHoverName().getString());
			}
		}
	}
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		this.playerWillDestroyBase(level, pos, state, player);
	}
	
	public void playerWillDestroyBase(Level level, BlockPos pos, BlockState state, Player player)
	{
		BlockEntity blockEntity = this.getTileEntity(state, level, pos);
		if(blockEntity instanceof TraderBlockEntity)
		{
			TraderBlockEntity trader = (TraderBlockEntity)blockEntity;
			if(!trader.canBreak(player))
				return;
			else
				trader.dumpContents(level, pos);
		}
		super.playerWillDestroy(level, pos, state, player);
	}
	
	@Override
	public BlockEntity getTileEntity(BlockState state, LevelAccessor world, BlockPos pos) {
		return world.getBlockEntity(pos);
	}
	
}
