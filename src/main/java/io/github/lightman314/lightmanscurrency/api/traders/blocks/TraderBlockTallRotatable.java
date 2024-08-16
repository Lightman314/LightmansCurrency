package io.github.lightman314.lightmanscurrency.api.traders.blocks;

import java.util.function.BiFunction;

import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.ITallBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TraderBlockTallRotatable extends TraderBlockRotatable implements ITallBlock {

	protected static final BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;
	private final BiFunction<Direction,Boolean,VoxelShape> shape;
	
	protected TraderBlockTallRotatable(Properties properties) { this(properties, LazyShapes.TALL_BOX_SHAPE); }
	
	protected TraderBlockTallRotatable(Properties properties, VoxelShape shape) { this(properties, LazyShapes.lazyTallSingleShape(shape)); }
	
	protected TraderBlockTallRotatable(Properties properties, BiFunction<Direction,Boolean,VoxelShape> shape)
	{
		super(properties.pushReaction(PushReaction.BLOCK));
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
	@Nonnull
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context)
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
	public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity player, @Nonnull ItemStack stack)
	{
		if(this.isReplaceable(level,pos.above()))
			level.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(ISBOTTOM, false).setValue(FACING, state.getValue(FACING)));
		else
		{
			//Flag as a legitimate break so that it won't break the other block as an illegal break...
			if(level.getBlockEntity(pos) instanceof TraderBlockEntity<?> be)
				be.flagAsLegitBreak();
			//Failed placing the top block. Abort placement
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			if(player instanceof Player p)
			{
				ItemStack giveStack = stack.copy();
				giveStack.setCount(1);
				InventoryUtil.safeGiveToPlayer(p.getInventory(),giveStack);
			}
		}
		
		this.setPlacedByBase(level, pos, state, player, stack);
		
	}

	@Deprecated
	public boolean getReplacable(Level level, BlockPos pos, BlockState ignored, LivingEntity player, ItemStack stack) { return level.getBlockState(pos).getBlock() == Blocks.AIR; }

	@Nonnull
	@Override
	public BlockState playerWillDestroy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player)
	{
		
		//Run base functionality first to prevent the removal of the block containing the block entity
		this.playerWillDestroyBase(level, pos, state, player);
		
		BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof TraderBlockEntity<?> trader)
		{
			if(!trader.canBreak(player))
                return state;
		}
		
		//Destroy the other half of the Tall Block
		setAir(level, this.getOtherHeight(pos, state), player);

        return state;
    }

	@Override
	public void removeOtherBlocks(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockPos pos) {
		setAir(level, this.getOtherHeight(pos, state), null);
	}
	
	@Nullable
	@Override
	public BlockEntity getBlockEntity(@Nonnull BlockState state, @Nonnull LevelAccessor level, @Nonnull BlockPos pos)
	{
		if(level == null)
			return null;
		if(this.getIsTop(state))
			return level.getBlockEntity(pos.below());
		return level.getBlockEntity(pos);
	}
	
}
