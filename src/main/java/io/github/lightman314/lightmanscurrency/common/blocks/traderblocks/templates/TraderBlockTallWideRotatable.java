package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates;

import java.util.function.BiFunction;

import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IWideBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.util.TriFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public abstract class TraderBlockTallWideRotatable extends TraderBlockTallRotatable implements IWideBlock{

	protected static final BooleanProperty ISLEFT = BlockStateProperties.ATTACHED;
	private final TriFunction<Direction,Boolean,Boolean, VoxelShape> shape;
	
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
	public @Nonnull VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader level, @Nonnull BlockPos pos, @Nonnull ISelectionContext context)
	{
		return this.shape.apply(this.getFacing(state), this.getIsBottom(state), this.getIsLeft(state));
	}
	
	@Override
    protected void createBlockStateDefinition(@Nonnull StateContainer.Builder<Block, BlockState> builder)
    {
		super.createBlockStateDefinition(builder);
        builder.add(ISLEFT);
    }
	
	@Override
	public BlockState getStateForPlacement(@Nonnull BlockItemUseContext context)
	{
		return super.getStateForPlacement(context).setValue(ISLEFT,true);
	}
	
	@Override
	public void setPlacedBy(@Nonnull World level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
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
			if(player instanceof PlayerEntity)
			{
				ItemStack giveStack = stack.copy();
				giveStack.setCount(1);
				((PlayerEntity)player).inventory.add(giveStack);
			}
		}
		
		this.setPlacedByBase(level, pos, state, player, stack);
		
	}
	
	@Override
	public void playerWillDestroy(@Nonnull World level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull PlayerEntity player)
	{
		//Run base functionality first to prevent the removal of the block containing the block entity
		this.playerWillDestroyBase(level, pos, state, player);
		
		TileEntity blockEntity = this.getBlockEntity(state, level, pos);
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
	protected void onInvalidRemoval(BlockState state, World level, BlockPos pos, TraderData trader) {
		super.onInvalidRemoval(state, level, pos, trader);
		BlockPos otherPos = this.getOtherSide(pos, state, this.getFacing(state));
		setAir(level, otherPos, null);
		setAir(level, this.getOtherHeight(otherPos, state), null);
	}
	
	@Override
	public TileEntity getBlockEntity(BlockState state, IWorld level, BlockPos pos)
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