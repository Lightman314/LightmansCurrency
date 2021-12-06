package io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates;

import java.util.function.BiFunction;

import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.ITallBlock;
import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class TraderBlockTallRotatable extends TraderBlockRotatable implements ITallBlock{

	protected static final BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;
	private final BiFunction<Direction,Boolean,VoxelShape> shape;
	
	
	protected TraderBlockTallRotatable(Properties properties)
	{
		this(properties, LazyShapes.TALL_BOX_SHAPE_T);
	}
	
	protected TraderBlockTallRotatable(Properties properties, VoxelShape shape)
	{
		this(properties, LazyShapes.lazyTallSingleShape(shape));
	}
	
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
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return this.shape.apply(this.getFacing(state), this.getIsBottom(state));
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
	
	@Override
	public PushReaction getPistonPushReaction(BlockState state)
	{
		return PushReaction.BLOCK;
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(level.getBlockState(pos.above()).getBlock() == Blocks.AIR)
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
		
		//Destroy the other half of the Tall Block
		setAir(level, this.getOtherHeight(pos, state), player);
		
		this.playerWillDestroyBase(level, pos, state, player);
		
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
		return level.getBlockEntity(pos);
	}
	
	@Override
	public boolean getIsBottom(BlockState state) { return state.getValue(ISBOTTOM); }
	
}
