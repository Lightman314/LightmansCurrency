package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates;

import java.util.function.BiFunction;

import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.ITallBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

public abstract class TraderBlockTallRotatable extends TraderBlockRotatable implements ITallBlock{

	private final BiFunction<Direction,Boolean,VoxelShape> shape;
	
	protected TraderBlockTallRotatable(Properties properties) { this(properties, LazyShapes.TALL_BOX_SHAPE_T); }
	
	protected TraderBlockTallRotatable(Properties properties, VoxelShape shape) { this(properties, LazyShapes.lazyTallSingleShape(shape)); }
	
	protected TraderBlockTallRotatable(Properties properties, BiFunction<Direction,Boolean,VoxelShape> shape)
	{
		super(properties);
		this.shape = shape;
		this.registerDefaultState(
			this.defaultBlockState()
				.setValue(FACING, Direction.NORTH)
				.setValue(ISBOTTOM, true)
		);
	}
	
	@Override
	protected boolean shouldMakeTrader(BlockState state) { return this.getIsBottom(state); }
	
	@Override
	public @Nonnull VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context)
	{
		return this.shape.apply(this.getFacing(state), this.getIsBottom(state));
	}
	
	@Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder)
    {
		super.createBlockStateDefinition(builder);
        builder.add(ISBOTTOM);
    }
	
	@Override
	public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context)
	{
		return super.getStateForPlacement(context).setValue(ISBOTTOM,true);
	}
	
	@Override
	public @Nonnull PushReaction getPistonPushReaction(@Nonnull BlockState state) {
		return PushReaction.BLOCK;
	}
	
	@Override
	public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
	{
		if(this.getReplacable(level, pos.above(), state, player, stack))
			level.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(ISBOTTOM, false).setValue(FACING, state.getValue(FACING)));
		else
		{
			//Failed placing the top block. Abort placement
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
			if(player instanceof Player)
			{
				ItemStack giveStack = stack.copy();
				giveStack.setCount(1);
				((Player)player).getInventory().add(giveStack);
			}
		}
		
		this.setPlacedByBase(level, pos, state, player, stack);
		
	}
	
	public boolean getReplacable(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack) {
		if(player instanceof Player)
		{
			BlockPlaceContext context = new BlockPlaceContext(level, (Player)player, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.ZERO, Direction.UP, pos, true));
			return level.getBlockState(pos).canBeReplaced(context);
		}
		else
		{
			return level.getBlockState(pos).getBlock() == Blocks.AIR;
		}
	}
	
	@Override
	public void playerWillDestroy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player)
	{
		
		//Run base functionality first to prevent the removal of the block containing the block entity
		this.playerWillDestroyBase(level, pos, state, player);
		
		BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof TraderBlockEntity<?> trader)
		{
			if(!trader.canBreak(player))
				return;
		}
		
		//Destroy the other half of the Tall Block
		setAir(level, this.getOtherHeight(pos, state), player);
		
	}
	
	@Override
	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderData trader) {
		super.onInvalidRemoval(state, level, pos, trader);
		//Destroy the other half of the Tall Block
		setAir(level, this.getOtherHeight(pos, state), null);
	}
	
	protected final void setAir(Level level, BlockPos pos, Player player)
	{
		BlockState state = level.getBlockState(pos);
		if(state.getBlock() == this)
		{
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
			if(player != null)
				level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
		}
	}
	
	@Override
	public BlockEntity getBlockEntity(BlockState state, LevelAccessor level, BlockPos pos)
	{
		if(level == null)
			return null;
		if(this.getIsTop(state))
			return level.getBlockEntity(pos.below());
		return level.getBlockEntity(pos);
	}
	
	@Override
	public boolean getIsBottom(BlockState state) { return state.getValue(ISBOTTOM); }
	
}