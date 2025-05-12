package io.github.lightman314.lightmanscurrency.api.misc.blocks;

import java.util.function.BiFunction;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

public class TallRotatableBlock extends RotatableBlock implements ITallBlock {

	private final BiFunction<Direction,Boolean,VoxelShape> shape;
	
	protected TallRotatableBlock(Properties properties) { this(properties, LazyShapes.TALL_BOX_SHAPE); }
	
	protected TallRotatableBlock(Properties properties, VoxelShape shape) { this(properties, LazyShapes.lazyTallSingleShape(shape)); }
	
	protected TallRotatableBlock(Properties properties, BiFunction<Direction,Boolean,VoxelShape> shape)
	{
		super(properties.pushReaction(PushReaction.BLOCK));
		this.shape = shape;
		this.registerDefaultState(
			this.defaultBlockState()
				.setValue(FACING, Direction.NORTH)
				.setValue(ISBOTTOM, true)
		);
	}
	
	@Nonnull
	@Override
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
	public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
	{
		if(this.isReplaceable(level, pos.above()))
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

		this.tryCopyVariant(level,pos,stack);
		
	}
	
	@Nonnull
	@SuppressWarnings("deprecation")
	@Override
	public BlockState updateShape(@Nonnull BlockState stateIn, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull LevelAccessor worldIn, @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos)
	{
		if((facing == Direction.UP && stateIn.getValue(ISBOTTOM)) || (facing == Direction.DOWN && !stateIn.getValue(ISBOTTOM)))
		{
			if(facingState.is(this))
			{
				return stateIn;
			}
			else
			{
				return Blocks.AIR.defaultBlockState();
			}
		}
		return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}
	
}
