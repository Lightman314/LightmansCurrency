package io.github.lightman314.lightmanscurrency.blocks;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.tileentity.FreezerTraderTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FreezerBlock extends RotatableBlock implements IItemTraderBlock{
	
	public static final int TRADECOUNT = 8;
	
	private static final VoxelShape SHAPE_SOUTH = makeCuboidShape(0d,0d,3d,16d,32d,16d);
	private static final VoxelShape SHAPE_NORTH = makeCuboidShape(0d,0d,0d,16d,32d,13d);
	private static final VoxelShape SHAPE_EAST = makeCuboidShape(3d,0d,0d,16d,32d,16d);
	private static final VoxelShape SHAPE_WEST = makeCuboidShape(0d,0d,0d,13d,32d,16d);
	
	protected static final BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;
	
	public FreezerBlock(Properties properties)
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
		
		Direction facing = this.getFacing(state);
		VoxelShape shape = SHAPE_SOUTH;
		if(facing == Direction.NORTH)
			shape = SHAPE_NORTH;
		else if(facing == Direction.EAST)
			shape = SHAPE_EAST;
		else if(facing == Direction.WEST)
			shape = SHAPE_WEST;
		
		if(state.get(ISBOTTOM))
			return shape;
		else
			return shape.withOffset(0d, -1d, 0d);
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
		
		//Prevent client-side multi-block destruction & breaking animations if they aren't allowed to break this trader
		TraderTileEntity tileEntity = (TraderTileEntity)getTileEntity(state, worldIn, pos);
		if(tileEntity != null)
		{
			if(!tileEntity.canBreak(player))
				return;
			else
				tileEntity.dumpContents(worldIn, pos);
		}
		
		//Destroy the other half of the ATM Machine
		if(state.get(ISBOTTOM))
		{
			//Get ATM block above and destroy it.
			BlockState upState = worldIn.getBlockState(pos.up());
			if(upState.getBlock() instanceof FreezerBlock)
			{
				worldIn.setBlockState(pos.up(), Blocks.AIR.getDefaultState(),35);
				worldIn.playEvent(player, 2001, pos.up(), Block.getStateId(upState));
			}
		}
		else
		{
			//Get ATM block below and destroy it.
			BlockState downState = worldIn.getBlockState(pos.down());
			if(downState.getBlock() instanceof FreezerBlock)
			{
				worldIn.setBlockState(pos.down(), Blocks.AIR.getDefaultState(),35);
				worldIn.playEvent(player, 2001, pos.up(), Block.getStateId(downState));
			}
		}
		
		super.onBlockHarvested(worldIn, pos, state, player);
		
	}
	
	@Override
	public PushReaction getPushReaction(BlockState state)
	{
		return PushReaction.BLOCK;
	}
	
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		worldIn.setBlockState(pos.up(), this.getDefaultState().with(ISBOTTOM, false).with(FACING, state.get(FACING)));
		if(!worldIn.isRemote())
		{
			ItemTraderTileEntity tileEntity = (ItemTraderTileEntity)worldIn.getTileEntity(pos);
			if(tileEntity != null)
			{
				tileEntity.setOwner(player);
				if(stack.hasDisplayName())
					tileEntity.setCustomName(stack.getDisplayName().getString());
			}
		}
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult result)
	{
		if(!world.isRemote())
		{
			//Open UI
			TileEntity tileEntity = getTileEntity(state, world, pos);
			if(tileEntity instanceof ItemTraderTileEntity)
			{
				ItemTraderTileEntity trader = (ItemTraderTileEntity)tileEntity;
				//Validate the trade count
				//if(trader.getTradeCount() != TRADECOUNT && !trader.isCreative())
				//	trader.overrideTradeCount(TRADECOUNT);
				//Update the owner
				if(trader.isOwner(playerEntity) && !trader.isCreative())
				{
					//CurrencyMod.LOGGER.info("Updating the owner name.");
					trader.setOwner(playerEntity);
				}
				TileEntityUtil.sendUpdatePacket(tileEntity);
				trader.openTradeMenu((ServerPlayerEntity)playerEntity);
			}
		}
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return state.get(ISBOTTOM);
	}
	
	public TileEntity getTileEntity(BlockState state, IWorld world, BlockPos pos)
	{
		if(state.get(ISBOTTOM))
			return world.getTileEntity(pos);
		else
			return world.getTileEntity(pos.down());
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new FreezerTraderTileEntity(TRADECOUNT);
	}
	
	@Override
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isBlock) {
		//Get facing
		Direction facing = this.getFacing(state);
		//Define directions for easy positional handling
		Vector3f forward = this.getForwardVect(facing);
		Vector3f right = this.getRightVect(facing);
		Vector3f up = Vector3f.YP;
		Vector3f offset = this.getOffsetVect(facing);
		
		Vector3f forwardOffset = MathUtil.VectorMult(forward, 6f/16f);
		
		Vector3f firstPosition = null;
		
		if(tradeSlot == 0)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 27f/16f);
			firstPosition = MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 1)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 27f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 2)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 20f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 3)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 20f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 4)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 13f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 5)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 13f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 6)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 6f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 7)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 6f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		
		List<Vector3f> posList = new ArrayList<>(3);
		if(firstPosition != null)
		{
			posList.add(firstPosition);
			for(float distance = isBlock ? 3.2f : 0.5f; distance < 7; distance += isBlock ? 3.2f : 0.5f)
			{
				posList.add(MathUtil.VectorAdd(firstPosition, MathUtil.VectorMult(forward, distance/16F)));
			}
		}
		else
		{
			posList.add(new Vector3f(0F, 1f, 0F));
		}
		return posList;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state, boolean isBlock)
	{
		List<Quaternion> rotation = new ArrayList<>();
		int facing = state.get(FACING).getHorizontalIndex();
		rotation.add(Vector3f.YP.rotationDegrees(facing * -90f));
		return rotation;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Vector3f GetStackRenderScale(int tradeSlot, BlockState state, boolean isBlock){
		return new Vector3f(0.4f, 0.4f, 0.4f);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex()
	{
		return TRADECOUNT;
	}
	
}
