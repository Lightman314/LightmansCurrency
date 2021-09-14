package io.github.lightman314.lightmanscurrency.blocks;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.containers.ATMContainer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ATMBlock extends RotatableBlock{
	
	private static final TranslatableComponent TITLE = new TranslatableComponent("gui.lightmanscurrency.atm.title");
	
	private static final BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;
	
	public ATMBlock(Properties properties)
	{
		super(properties, null);
		this.registerDefaultState(
			this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(ISBOTTOM, true)
		);
	}
	
	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return Shapes.empty();
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		if(state.getValue(ISBOTTOM))
			return LazyShapes.TALL_BOX_T;
		return LazyShapes.moveDown(LazyShapes.TALL_BOX_T);
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
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		if(!level.isClientSide && player.isCreative())
		{
			//Destroy the other half of the ATM Machine
			if(!state.getValue(ISBOTTOM))
			{
				//Get ATM block below and destroy it.
				BlockState downState = level.getBlockState(pos.below());
				if(downState.getBlock() instanceof ATMBlock)
				{
					level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(),35);
					level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos.below());
				}
			}
		}
		
		super.playerWillDestroy(level, pos, state, player);
		
	}
	
	@Override
	public PushReaction getPistonPushReaction(BlockState state)
	{
		return PushReaction.BLOCK;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos)
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
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(level.getBlockState(pos.above()).getBlock() == Blocks.AIR)
			level.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(ISBOTTOM, false).setValue(FACING, state.getValue(FACING)));
		else
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		player.openMenu(state.getMenuProvider(level, pos));
		return InteractionResult.SUCCESS;
	}
	
	@Nullable
	@Override
	public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos)
	{
		return new SimpleMenuProvider((windowId, playerInventory, playerEntity) -> { return new ATMContainer(windowId, playerInventory);}, TITLE);
	}
	
	
}
