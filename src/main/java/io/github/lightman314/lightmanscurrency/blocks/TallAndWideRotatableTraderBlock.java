package io.github.lightman314.lightmanscurrency.blocks;

import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.blockentity.DummyBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
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

public abstract class TallAndWideRotatableTraderBlock extends TallRotatableTraderBlock{
	
	protected static final BooleanProperty ISLEFT = BlockStateProperties.ATTACHED;
	
	protected TallAndWideRotatableTraderBlock(Properties properties, int tradeCount)
	{
		super(properties, tradeCount);
	}
	
	@Override
	protected BlockState defineDefaultState(BlockState state)
	{
		return super.defineDefaultState(state).setValue(ISLEFT, true);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		if(state.getValue(ISBOTTOM) && state.getValue(ISLEFT))
			return new ItemTraderBlockEntity(pos, state, TRADE_COUNT);
		return new DummyBlockEntity(pos, state);
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
		return super.getStateForPlacement(context).setValue(ISLEFT, true);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		Direction direction = state.getValue(FACING);
		Vector3f offset = new Vector3f(0f,0f,0f);
		VoxelShape shape = makeShape(direction);
		
		if(state.getValue(ISBOTTOM))
		{
			if(state.getValue(ISLEFT))
			{
				//No offset
			}
			else
			{
				offset = getLeftOffset(direction);
			}
		}
		else
		{
			if(state.getValue(ISLEFT))
			{
				offset = new Vector3f(0f,-1f,0f);
			}
			else
			{
				offset = MathUtil.VectorAdd(getLeftOffset(direction), new Vector3f(0f,-1f,0f));
			}
		}
		return shape.move(offset.x(), offset.y(), offset.z());
	}
	
	private VoxelShape makeShape(Direction direction)
	{
		if(this.isTransparent())
		{
			switch(direction)
			{
				case EAST:
					return box(0.01d,0d,0.01d,15.99d,32d,31.99d);
				case SOUTH:
					return box(-15.99d,0d,0.01d,15.99d,32d,15.99d);
				case WEST:
					return box(0.01d,0d,-15.99d,15.99d,32d,15.99d);
				default:
					return box(0.01d,0d,0.01d,31.99d,32d,15.99d);
			}
		}
		else
		{
			switch(direction)
			{
				case EAST:
					return box(0d,0d,0d,16d,32d,32d);
				case SOUTH:
					return box(-16d,0d,0d,16d,32d,16d);
				case WEST:
					return box(0d,0d,-16d,16d,32d,16d);
				default:
					return box(0d,0d,0.d,32d,32d,16d);
			}
		}
	}
	
	private Vector3f getLeftOffset(Direction direction)
	{
		if(direction == Direction.NORTH)
			return new Vector3f(-1f,0f,0f);
		if(direction == Direction.SOUTH)
			return new Vector3f(1f,0f,0f);
		if(direction == Direction.EAST)
			return new Vector3f(0f,0f, -1f);
		if(direction == Direction.WEST)
			return new Vector3f(0f,0f,1f);
		return new Vector3f(0f,0f,0f);
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
		{
			setAir(level, pos.above(), player);
			BlockPos otherPos = pos;
			if(state.getValue(ISLEFT))
				otherPos = getRightPos(state, pos);
			else
				otherPos = getLeftPos(state, pos);
			
			setAir(level, otherPos, player);
			setAir(level, otherPos.above(), player);
		}
		else
		{
			setAir(level, pos.below(), player);
			BlockPos otherPos = pos;
			if(state.getValue(ISLEFT))
				otherPos = getRightPos(state, pos);
			else
				otherPos = getLeftPos(state, pos);
			
			setAir(level, otherPos, player);
			setAir(level, otherPos.below(), player);
		}
		
		
		superPlayerWillDestroy(level, pos, state, player);
		
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		Player playerEntity = (Player)player;
		ItemStack giveStack = stack.copy();
		giveStack.setCount(1);
		
		BlockPos rightPos = getRightPos(state, pos);
		
		//Attempt to place the other three blocks
		if(level.getBlockState(rightPos).getBlock() == Blocks.AIR && level.getBlockState(rightPos.above()).getBlock() == Blocks.AIR && level.getBlockState(pos.above()).getBlock() == Blocks.AIR)
		{
			level.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(ISBOTTOM, false).setValue(FACING, state.getValue(FACING)).setValue(ISLEFT, true));
			level.setBlockAndUpdate(rightPos, this.defaultBlockState().setValue(ISBOTTOM, true).setValue(FACING, state.getValue(FACING)).setValue(ISLEFT, false));
			level.setBlockAndUpdate(rightPos.above(), this.defaultBlockState().setValue(ISBOTTOM, false).setValue(FACING, state.getValue(FACING)).setValue(ISLEFT, false));
		}
		else
		{
			//Failed placing one of the blocks. Abort placement and refund the item.
			if(!playerEntity.isCreative())
				playerEntity.getInventory().add(giveStack);
			
			level.setBlock(pos, Blocks.AIR.defaultBlockState(),35);
			
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
	public BlockEntity getTileEntity(BlockState state, LevelAccessor level, BlockPos pos)
	{
		BlockPos getPos = pos;
		if(!state.getValue(ISLEFT))
			getPos = getLeftPos(state, pos);
		
		if(state.getValue(ISBOTTOM))
			return level.getBlockEntity(getPos);
		else
			return level.getBlockEntity(getPos.below());
	}

	
	private BlockPos getLeftPos(BlockState state, BlockPos pos)
	{
		if(state.getValue(FACING) == Direction.NORTH)
		{
			return new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
		}
		else if(state.getValue(FACING) == Direction.SOUTH)
		{
			return new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
		}
		else if(state.getValue(FACING) == Direction.EAST)
		{
			return new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
		}
		else if(state.getValue(FACING) == Direction.WEST)
		{
			return new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
		}
		return pos;
	}
	
	private BlockPos getRightPos(BlockState state, BlockPos pos)
	{
		if(state.getValue(FACING) == Direction.NORTH)
		{
			return new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
		}
		else if(state.getValue(FACING) == Direction.SOUTH)
		{
			return new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
		}
		else if(state.getValue(FACING) == Direction.EAST)
		{
			return new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
		}
		else if(state.getValue(FACING) == Direction.WEST)
		{
			return new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
		}
		return pos;
	}
}
