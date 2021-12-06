package io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates;

import java.util.function.BiFunction;

import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IWideBlock;
import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes.TriFunction;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
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
		if(level.getBlockState(rightPos).getBlock() == Blocks.AIR && level.getBlockState(rightPos.above()).getBlock() == Blocks.AIR && level.getBlockState(pos.above()).getBlock() == Blocks.AIR)
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
		
		this.setPlacedByBase(level, rightPos, state, player, stack);
		
	}
	
	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		BlockEntity blockEntity = this.getTileEntity(state, level, pos);
		if(blockEntity instanceof TraderTileEntity)
		{
			TraderTileEntity trader = (TraderTileEntity)blockEntity;
			if(!trader.canBreak(player))
				return;
		}
		
		if(state.getValue(ISBOTTOM))
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
		
		super.playerWillDestroyBase(level, pos, state, player);
		
	}
	
	protected void setAir(Level level, BlockPos pos, Player player)
	{
		BlockState state = level.getBlockState(pos);
		if(state.getBlock().getClass() == this.getClass())
		{
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
			level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
		}
	}
	
	@Override
	public BlockEntity getTileEntity(BlockState state, LevelAccessor level, BlockPos pos)
	{
		if(!this.getIsBottom(state))
			pos = pos.below();
		if(!this.getIsLeft(state))
			pos = IRotatableBlock.getLeftPos(pos, this.getFacing(state));
		return level.getBlockEntity(pos);
	}
	
	@Override
	public boolean getIsLeft(BlockState state) { return state.getValue(ISLEFT); }
	
}
