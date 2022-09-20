package io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates;

import java.util.function.BiFunction;

import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IWideBlock;
import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes.TriFunction;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class TraderBlockTallWideRotatable extends TraderBlockTallRotatable implements IWideBlock{

	protected static final BooleanProperty ISLEFT = BlockStateProperties.ATTACHED;
	private final TriFunction<Direction,Boolean,Boolean,VoxelShape> shape;
	
	protected TraderBlockTallWideRotatable(Properties properties)
	{
		this(properties, LazyShapes.TALL_WIDE_BOX_SHAPE_T);
	}
	
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
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return this.shape.apply(this.getFacing(state), this.getIsBottom(state), this.getIsLeft(state));
	}
	
	@Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
		super.createBlockStateDefinition(builder);
        builder.add(ISLEFT);
    }
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return super.getStateForPlacement(context).setValue(ISLEFT,true);
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		//Attempt to place the other three blocks
		BlockPos rightPos = IRotatableBlock.getRightPos(pos, this.getFacing(state));
		if(this.getReplacable(level, rightPos, state, player, stack) && this.getReplacable(level, rightPos.above(), state, player, stack) && this.getReplacable(level, pos.above(), state, player, stack))
		{
			level.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(ISBOTTOM, false).setValue(FACING, state.getValue(FACING)).setValue(ISLEFT, true));
			level.setBlockAndUpdate(rightPos, this.defaultBlockState().setValue(ISBOTTOM, true).setValue(FACING, state.getValue(FACING)).setValue(ISLEFT, false));
			level.setBlockAndUpdate(rightPos.above(), this.defaultBlockState().setValue(ISBOTTOM, false).setValue(FACING, state.getValue(FACING)).setValue(ISLEFT, false));
		}	
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
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		//Run base functionality first to prevent the removal of the block containing the block entity
		this.playerWillDestroyBase(level, pos, state, player);
		
		BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof TraderBlockEntity<?>)
		{
			TraderBlockEntity<?> trader = (TraderBlockEntity<?>)blockEntity;
			if(!trader.canBreak(player))
				return;
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
		
	}
	
	@Override
	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderData trader) {
		super.onInvalidRemoval(state, level, pos, trader);
		BlockPos otherPos = this.getOtherSide(pos, state, this.getFacing(state));
		setAir(level, otherPos, null);
		setAir(level, this.getOtherHeight(otherPos, state), null);
	}
	
	@Override
	public BlockEntity getBlockEntity(BlockState state, LevelAccessor level, BlockPos pos)
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
	
	@Override
	public boolean getIsLeft(BlockState state) { return state.getValue(ISLEFT); }
	
}