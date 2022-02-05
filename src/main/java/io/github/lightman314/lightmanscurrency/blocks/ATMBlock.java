package io.github.lightman314.lightmanscurrency.blocks;

//import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.containers.ATMContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
//import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
//import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
//import net.minecraft.world.server.ServerWorld;

public class ATMBlock extends RotatableBlock{
	
	//private static final TranslationTextComponent TITLE = new TranslationTextComponent("gui.lightmanscurrency.atm.title");
	
	private static final VoxelShape SHAPE_BOTTOM = makeCuboidShape(0d,0d,0d,16d,32d,16d);
	private static final VoxelShape SHAPE_TOP = makeCuboidShape(0d,-16d,0d,16d,16d,16d);
	
	static final BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;
	
	
	public ATMBlock(Properties properties)
	{
		super(properties, null);
		this.setDefaultState(
			this.stateContainer.getBaseState()
				.with(FACING, Direction.NORTH)
				.with(ISBOTTOM, true)
		);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext contect)
	{
		if(state.get(ISBOTTOM))
			return SHAPE_BOTTOM;
		else
			return SHAPE_TOP;
	}
	
	@Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        super.fillStateContainer(builder);
        builder.add(ISBOTTOM);
    }
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return super.getStateForPlacement(context).with(ISBOTTOM,true);
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader reader, BlockPos pos)
	{
		return reader.getBlockState(pos.up()).getBlock() == Blocks.AIR;
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
	{
		if(!worldIn.isRemote && player.isCreative())
		{
			//Destroy the other half of the ATM Machine
			if(!state.get(ISBOTTOM))
			{
				//Get ATM block below and destroy it.
				BlockState downState = worldIn.getBlockState(pos.down());
				if(downState.getBlock() instanceof ATMBlock)
				{
					worldIn.setBlockState(pos.down(), Blocks.AIR.getDefaultState(),35);
					worldIn.playEvent(player, 2001, pos.down(), Block.getStateId(downState));
				}
			}
		}
		
		super.onBlockHarvested(worldIn, pos, state, player);
		
	}
	
	@Override
	public PushReaction getPushReaction(BlockState state)
	{
		return PushReaction.BLOCK;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		
		if((facing == Direction.UP && stateIn.get(ISBOTTOM)) || (facing == Direction.DOWN && !stateIn.get(ISBOTTOM)))
		{
			if(facingState.isIn(this))
			{
				return stateIn;
			}
			else
			{
				return Blocks.AIR.getDefaultState();
			}
		}
		
		return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		worldIn.setBlockState(pos.up(), this.getDefaultState().with(ISBOTTOM, false).with(FACING, state.get(FACING)));
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult result)
	{
		playerEntity.openContainer(state.getContainer(world, pos));
		return ActionResultType.SUCCESS;
	}
	
	@Nullable
	@Override
	public INamedContainerProvider getContainer(BlockState state, World world, BlockPos pos)
	{
		return new SimpleNamedContainerProvider((windowId, playerInventory, playerEntity) -> { return new ATMContainer(windowId, playerInventory);}, new StringTextComponent(""));
	}
	
	
}
