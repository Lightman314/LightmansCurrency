package io.github.lightman314.lightmanscurrency.api.traders.blocks;

import java.util.function.BiFunction;

import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.ITallBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;

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
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity player, ItemStack stack)
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
				ItemHandlerHelper.giveItemToPlayer(p,giveStack);
			}
		}
		this.setPlacedByBase(level,pos,state,player,stack);
	}

	@Deprecated
	public boolean getReplacable(Level level, BlockPos pos, BlockState ignored, LivingEntity player, ItemStack stack) { return level.getBlockState(pos).getBlock() == Blocks.AIR; }

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
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
	public void removeOtherBlocks(Level level, BlockState state, BlockPos pos) {
		setAir(level, this.getOtherHeight(pos, state), null);
	}
	
}
