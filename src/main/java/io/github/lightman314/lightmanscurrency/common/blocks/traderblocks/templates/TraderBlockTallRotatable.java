package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates;

import java.util.function.BiFunction;

import io.github.lightman314.lightmanscurrency.common.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.ITallBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public abstract class TraderBlockTallRotatable extends TraderBlockRotatable implements ITallBlock{

	protected static final BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;
	private final BiFunction<Direction,Boolean, VoxelShape> shape;
	
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
	public @Nonnull VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader level, @Nonnull BlockPos pos, @Nonnull ISelectionContext context)
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
	
	@Override
	public @Nonnull PushReaction getPistonPushReaction(@Nonnull BlockState state)
	{
		return PushReaction.BLOCK;
	}
	
	@Override
	public void setPlacedBy(@Nonnull World level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
	{
		if(this.getReplacable(level, pos.above(), state, player, stack))
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
		
		this.setPlacedByBase(level, pos, state, player, stack);
		
	}
	
	public boolean getReplacable(World level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack) {
		if(player instanceof PlayerEntity)
		{
			BlockItemUseContext context = new BlockItemUseContext(level, (PlayerEntity) player, Hand.MAIN_HAND, stack, new BlockRayTraceResult(Vector3d.ZERO, Direction.UP, pos, true));
			return level.getBlockState(pos).canBeReplaced(context);
		}
		else
		{
			return level.getBlockState(pos).getBlock() == Blocks.AIR;
		}
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
		
		//Destroy the other half of the Tall Block
		setAir(level, this.getOtherHeight(pos, state), player);
		
	}
	
	@Override
	protected void onInvalidRemoval(BlockState state, World level, BlockPos pos, TraderData trader) {
		super.onInvalidRemoval(state, level, pos, trader);
		//Destroy the other half of the Tall Block
		setAir(level, this.getOtherHeight(pos, state), null);
	}
	
	protected final void setAir(World level, BlockPos pos, PlayerEntity player)
	{
		BlockState state = level.getBlockState(pos);
		if(state.getBlock() == this)
		{
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
		}
	}
	
	@Override
	public TileEntity getBlockEntity(BlockState state, IWorld level, BlockPos pos)
	{
		if(level == null)
			return null;
		if(this.getIsTop(state))
			return level.getBlockEntity(pos.below());
		return level.getBlockEntity(pos);
	}
	
	@Override
	public boolean getIsBottom(BlockState state) { return state.getValue(ISBOTTOM); }
	
}