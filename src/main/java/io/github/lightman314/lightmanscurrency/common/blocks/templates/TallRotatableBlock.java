package io.github.lightman314.lightmanscurrency.common.blocks.templates;

import java.util.function.BiFunction;

import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.ITallBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TallRotatableBlock extends RotatableBlock implements ITallBlock{

	public static final BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;
	private final BiFunction<Direction,Boolean, VoxelShape> shape;
	
	protected TallRotatableBlock(Properties properties)
	{
		this(properties, LazyShapes.TALL_BOX_SHAPE_T);
	}
	
	protected TallRotatableBlock(Properties properties, VoxelShape shape)
	{
		this(properties, LazyShapes.lazyTallSingleShape(shape));
	}
	
	protected TallRotatableBlock(Properties properties, BiFunction<Direction,Boolean,VoxelShape> shape)
	{
		super(properties);
		this.shape = shape;
		this.registerDefaultState(
			this.defaultBlockState()
				.setValue(FACING, Direction.NORTH)
				.setValue(ISBOTTOM, true)
		);
	}
	
	@Nonnull
	@Override
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader level, @Nonnull BlockPos pos, @Nonnull ISelectionContext context)
	{
		return this.shape.apply(this.getFacing(state), this.getIsBottom(state));
	}
	
	@Override
    protected void createBlockStateDefinition(@Nonnull StateContainer.Builder<Block, BlockState> builder)
    {
		super.createBlockStateDefinition(builder);
        builder.add(ISBOTTOM);
    }
	
	@Override
	public BlockState getStateForPlacement(@Nonnull BlockItemUseContext context)
	{
		return super.getStateForPlacement(context).setValue(ISBOTTOM,true);
	}
	
	@Nonnull
	@Override
	public PushReaction getPistonPushReaction(@Nonnull BlockState state) { return PushReaction.BLOCK; }
	
	@Override
	public void setPlacedBy(World level, BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
	{
		if(level.getBlockState(pos.above()).getBlock() == Blocks.AIR)
			level.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(ISBOTTOM, false).setValue(FACING, state.getValue(FACING)));
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
		
	}
	
	@Nonnull
	@SuppressWarnings("deprecation")
	@Override
	public BlockState updateShape(@Nonnull BlockState stateIn, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull IWorld worldIn, @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos)
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
	
	@Override
	public boolean getIsBottom(BlockState state) { return state.getValue(ISBOTTOM); }
	
}
