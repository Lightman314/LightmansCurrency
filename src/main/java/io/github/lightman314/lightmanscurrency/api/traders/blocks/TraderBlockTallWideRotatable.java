package io.github.lightman314.lightmanscurrency.api.traders.blocks;

import java.util.function.BiFunction;

import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IWideBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.TriFunction;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TraderBlockTallWideRotatable extends TraderBlockTallRotatable implements IWideBlock{

	private final TriFunction<Direction,Boolean,Boolean,VoxelShape> shape;
	
	protected TraderBlockTallWideRotatable(Properties properties) { this(properties, LazyShapes.TALL_WIDE_BOX_SHAPE); }
	
	protected TraderBlockTallWideRotatable(Properties properties, VoxelShape north, VoxelShape east, VoxelShape south, VoxelShape west)
	{
		this(properties, LazyShapes.lazyTallWideDirectionalShape(north, east, south, west));
	}
	
	protected TraderBlockTallWideRotatable(Properties properties, BiFunction<Direction,Boolean,VoxelShape> tallShape)
	{
		this(properties, LazyShapes.lazyTallWideDirectionalShape(tallShape));
	}
	
	protected TraderBlockTallWideRotatable(Properties properties, TriFunction<Direction,Boolean,Boolean,VoxelShape> shape)
	{
		super(properties);
		this.shape = shape;
		this.registerDefaultState(
			this.defaultBlockState()
				.setValue(FACING, Direction.NORTH)
				.setValue(ISBOTTOM, true)
				.setValue(ISLEFT, true)
		);
	}
	
	@Override
	protected boolean shouldMakeTrader(BlockState state) { return this.getIsBottom(state) && this.getIsLeft(state); }
	
	@Override
	@Nonnull
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context)
	{
		return this.shape.apply(this.getFacing(state), this.getIsBottom(state), this.getIsLeft(state));
	}
	
	@Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder)
    {
		super.createBlockStateDefinition(builder);
        builder.add(ISLEFT);
    }
	
	@Override
	public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context)
	{
		return super.getStateForPlacement(context).setValue(ISLEFT,true);
	}
	
	@Override
	public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
	{
		//Attempt to place the other three blocks
		BlockPos rightPos = IRotatableBlock.getRightPos(pos, this.getFacing(state));
		if(this.isReplaceable(level, rightPos) && this.isReplaceable(level, rightPos.above()) && this.isReplaceable(level, pos.above()))
		{
			level.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(ISBOTTOM, false).setValue(FACING, state.getValue(FACING)).setValue(ISLEFT, true));
			level.setBlockAndUpdate(rightPos, this.defaultBlockState().setValue(ISBOTTOM, true).setValue(FACING, state.getValue(FACING)).setValue(ISLEFT, false));
			level.setBlockAndUpdate(rightPos.above(), this.defaultBlockState().setValue(ISBOTTOM, false).setValue(FACING, state.getValue(FACING)).setValue(ISLEFT, false));
		}	
		else
		{
			//Flag as a legitimate break so that it won't break the other block as an illegal break...
			if(level.getBlockEntity(pos) instanceof TraderBlockEntity<?> be)
				be.flagAsLegitBreak();
			//Failed placing the top block. Abort placement
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
			if(player instanceof Player p)
			{
				ItemStack giveStack = stack.copy();
				giveStack.setCount(1);
				InventoryUtil.safeGiveToPlayer(p.getInventory(),giveStack);
			}
		}
		
		this.setPlacedByBase(level, pos, state, player, stack);
		
	}
	
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
		
		if(this.getIsBottom(state))
		{
			setAir(level, pos.above(), player);
			BlockPos otherPos = this.getOtherSide(pos, state, this.getFacing(state));
			setAir(level, otherPos, player);
			setAir(level, otherPos.above(), player);
		}
		else
		{
			setAir(level, pos.below(), player);
			BlockPos otherPos = this.getOtherSide(pos, state, this.getFacing(state));
			setAir(level, otherPos, player);
			setAir(level, otherPos.below(), player);
		}

        return state;
    }

	@Override
	public void removeOtherBlocks(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockPos pos) {
		BlockPos otherPos = this.getOtherSide(pos, state, this.getFacing(state));
		setAir(level, otherPos, null);
		setAir(level, this.getOtherHeight(otherPos, state), null);
	}
	
	@Nullable
	@Override
	public BlockEntity getBlockEntity(@Nonnull BlockState state, @Nonnull LevelAccessor level, @Nonnull BlockPos pos)
	{
		if(level == null)
			return null;
		BlockPos getPos = pos;
		if(this.getIsRight(state))
			getPos = IRotatableBlock.getLeftPos(getPos, this.getFacing(state));
		if(this.getIsTop(state))
			return level.getBlockEntity(getPos.below());
		return level.getBlockEntity(getPos);
	}
	
}
