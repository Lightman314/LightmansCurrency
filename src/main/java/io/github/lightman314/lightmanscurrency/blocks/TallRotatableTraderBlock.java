package io.github.lightman314.lightmanscurrency.blocks;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.blockentity.DummyBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TickableBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.blocks.util.TickerUtil;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class TallRotatableTraderBlock extends RotatableBlock implements IItemTraderBlock, EntityBlock{

	protected final int TRADE_COUNT;
	private boolean isTransparent = false;
	protected boolean isTransparent() { return this.isTransparent; }
	protected static final BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;
	
	protected TallRotatableTraderBlock(Properties properties, int tradeCount) { this(properties, tradeCount, false); }
	
	protected TallRotatableTraderBlock(Properties properties, int tradeCount, boolean isWide)
	{
		super(properties);
		TRADE_COUNT = tradeCount;
		this.registerDefaultState(this.defineDefaultState(this.stateDefinition.any()));
	}
	
	protected void flagAsTransparent() { this.isTransparent = true; }
	
	protected BlockState defineDefaultState(BlockState state)
	{
		return state.setValue(FACING, Direction.NORTH).setValue(ISBOTTOM, true);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		if(state.getValue(ISBOTTOM))
			return new ItemTraderBlockEntity(pos, state, TRADE_COUNT);
		return new DummyBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return TickerUtil.createTickerHelper(type, ModBlockEntities.ITEM_TRADER, TickableBlockEntity::tickHandler);
	}
	
	@Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
		super.createBlockStateDefinition(builder);
        builder.add(ISBOTTOM);
    }
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return super.getStateForPlacement(context).setValue(ISBOTTOM,true);
	}
	
	private VoxelShape getShape()
	{
		if(this.isTransparent)
			return LazyShapes.TALL_BOX_T;
		return LazyShapes.TALL_BOX;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		if(state.getValue(ISBOTTOM))
			return getShape();
		return LazyShapes.moveDown(getShape());
	}
	
	@Override
	public PushReaction getPistonPushReaction(BlockState state)
	{
		return PushReaction.BLOCK;
	}
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		
		//Prevent client-side multi-block destruction & breaking animations if they aren't allowed to break this trader
		TraderBlockEntity blockEntity = (TraderBlockEntity)getTileEntity(state, level, pos);
		if(this.playerCanBreak(level, pos, state, player))
			blockEntity.dumpContents(level, pos);
		
		//Destroy the other half of the Tall Block
		if(state.getValue(ISBOTTOM))
			setAir(level, pos.above(), player);
		else
			setAir(level, pos.below(), player);
		
		super.playerWillDestroy(level, pos, state, player);
		
	}
	
	protected final void superPlayerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		super.playerWillDestroy(level, pos, state, player);
	}
	
	protected boolean playerCanBreak(Level level, BlockPos pos, BlockState state, Player player)
	{
		TraderBlockEntity blockEntity = (TraderBlockEntity)getTileEntity(state, level, pos);
		if(blockEntity != null)
		{
			if(!blockEntity.canBreak(player))
				return false;
			else
				return true;
		}
		return true;
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		Player playerEntity = (Player)player;
		ItemStack giveStack = stack.copy();
		giveStack.setCount(1);
		
		if(level.getBlockState(pos.above()).getBlock() == Blocks.AIR)
			level.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(ISBOTTOM, false).setValue(FACING, state.getValue(FACING)));
		else
		{
			//Failed placing the top block. Abort placement
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
			
			if(!playerEntity.isCreative())
				playerEntity.getInventory().add(giveStack);
			
			return;
		}
		
		if(!level.isClientSide)
		{
			ItemTraderBlockEntity blockEntity = (ItemTraderBlockEntity)level.getBlockEntity(pos);
			if(blockEntity != null)
			{
				blockEntity.setOwner(player);
				if(stack.hasCustomHoverName())
					blockEntity.setCustomName(stack.getDisplayName().getString());
			}
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			//Open UI
			BlockEntity blockEntity = this.getTileEntity(state, level, pos);
			if(blockEntity instanceof ItemTraderBlockEntity)
			{
				ItemTraderBlockEntity trader = (ItemTraderBlockEntity)blockEntity;
				//Update the owner
				if(trader.isOwner(player) && !trader.isCreative())
				{
					//CurrencyMod.LOGGER.info("Updating the owner name.");
					trader.setOwner(player);
				}
				TileEntityUtil.sendUpdatePacket(blockEntity);
				trader.openTradeMenu(player);
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	public BlockEntity getTileEntity(BlockState state, LevelAccessor level, BlockPos pos)
	{
		if(state.getValue(ISBOTTOM))
			return level.getBlockEntity(pos);
		else
			return level.getBlockEntity(pos.below());
	}
	
	protected void setAir(Level world, BlockPos pos, Player player)
	{
		BlockState state = world.getBlockState(pos);
		if(state.getBlock().getClass() == this.getClass())
		{
			world.setBlock(pos, Blocks.AIR.defaultBlockState(),35);
			world.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
		}
	}
	
}
