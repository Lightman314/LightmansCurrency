package io.github.lightman314.lightmanscurrency.blocks;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.tileentity.ItemInterfaceTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.ItemInterfaceTileEntity.IItemHandlerTileEntity;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
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

public class VendingMachineLargeBlock extends RotatableItemBlock implements IItemTraderBlock{
	
	public static final int TRADECOUNT = 12;
	
	protected static final BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;
	protected static final BooleanProperty ISLEFT = BlockStateProperties.ATTACHED;
	
	public VendingMachineLargeBlock(Properties properties)
	{
		super(properties, null);
		this.setDefaultState(
			this.stateContainer.getBaseState()
				.with(FACING, Direction.NORTH)
				.with(ISBOTTOM, true)
				.with(ISLEFT, false)
		);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext contect)
	{
		Direction direction = state.get(FACING);
		Vector3f offset = new Vector3f(0f,0f,0f);
		VoxelShape shape = makeShape(direction);
		
		if(state.get(ISBOTTOM))
		{
			if(state.get(ISLEFT))
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
			if(state.get(ISLEFT))
			{
				offset = new Vector3f(0f,-1f,0f);
			}
			else
			{
				offset = MathUtil.VectorAdd(getLeftOffset(direction), new Vector3f(0f,-1f,0f));
			}
		}
		return shape.withOffset(offset.getX(), offset.getY(), offset.getZ());
	}
	
	private VoxelShape makeShape(Direction direction)
	{
		if(direction == Direction.NORTH)
		{
			return makeCuboidShape(0d,0d,0d,32d,32d,16d);
		}
		else if(direction == Direction.SOUTH)
		{
			return makeCuboidShape(-16d,0d,0d,16d,32d,16d);
		}
		else if(direction == Direction.EAST)
		{
			return makeCuboidShape(0d,0d,0d,16d,32d,32d);
		}
		else if(direction == Direction.WEST)
		{
			return makeCuboidShape(0d,0d,-16d,16d,32d,16d);
		}
		
		return makeCuboidShape(0d,0d,0d,16d,16d,16d);
	}
	
	@Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        super.fillStateContainer(builder);
        builder.add(ISBOTTOM);
        builder.add(ISLEFT);
    }
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return super.getStateForPlacement(context).with(ISBOTTOM,true).with(ISLEFT, true);
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
		ItemTraderTileEntity tileEntity = (ItemTraderTileEntity)getTileEntity(state, worldIn, pos);
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
			//Get Vending machine block below and destroy it.
			setAir(worldIn, pos.up(), player);
			BlockPos otherPos = pos;
			if(state.get(ISLEFT))
				otherPos = getRightPos(state, pos);
			else
				otherPos = getLeftPos(state, pos);
			setAir(worldIn, otherPos, player);
			setAir(worldIn, otherPos.up(), player);
		}
		else
		{
			//Get Vending machine block below and destroy it.
			setAir(worldIn, pos.down(), player);
			BlockPos otherPos = pos;
			if(state.get(ISLEFT))
				otherPos = getRightPos(state, pos);
			else
				otherPos = getLeftPos(state, pos);
			setAir(worldIn, otherPos, player);
			setAir(worldIn, otherPos.down(), player);
		}
		
		super.onBlockHarvested(worldIn, pos, state, player);
		
	}

	@Override
	public PushReaction getPushReaction(BlockState state)
	{
		return PushReaction.BLOCK;
	}
	
	private void setAir(World world, BlockPos pos, PlayerEntity player)
	{
		BlockState state = world.getBlockState(pos);
		if(state.getBlock() instanceof VendingMachineLargeBlock)
		{
			world.setBlockState(pos, Blocks.AIR.getDefaultState(),35);
			world.playEvent(player, 2001, pos, Block.getStateId(state));
		}
	}
	
	private BlockPos getLeftPos(BlockState state, BlockPos pos)
	{
		if(state.get(FACING) == Direction.NORTH)
		{
			return new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
		}
		else if(state.get(FACING) == Direction.SOUTH)
		{
			return new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
		}
		else if(state.get(FACING) == Direction.EAST)
		{
			return new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
		}
		else if(state.get(FACING) == Direction.WEST)
		{
			return new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
		}
		return pos;
	}
	
	private BlockPos getRightPos(BlockState state, BlockPos pos)
	{
		if(state.get(FACING) == Direction.NORTH)
		{
			return new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
		}
		else if(state.get(FACING) == Direction.SOUTH)
		{
			return new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
		}
		else if(state.get(FACING) == Direction.EAST)
		{
			return new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
		}
		else if(state.get(FACING) == Direction.WEST)
		{
			return new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
		}
		return pos;
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
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		
		PlayerEntity playerEntity = (PlayerEntity)player;
		ItemStack giveStack = stack.copy();
		giveStack.setCount(1);
		
		BlockPos rightPos = getRightPos(state, pos);
		
		//Attempt to place the right two blocks
		if(worldIn.getBlockState(rightPos).getBlock() == Blocks.AIR && worldIn.getBlockState(rightPos.up()).getBlock() == Blocks.AIR)
		{
			worldIn.setBlockState(rightPos, this.getDefaultState().with(ISBOTTOM, true).with(FACING, state.get(FACING)).with(ISLEFT, false));
			worldIn.setBlockState(rightPos.up(), this.getDefaultState().with(ISBOTTOM, false).with(FACING, state.get(FACING)).with(ISLEFT, false));
		}
		else
		{
			//Failed placing the first block
			LightmansCurrency.LogInfo("Failed placing the lower-right block. Aborting placement.");
			
			if(!playerEntity.isCreative())
				playerEntity.inventory.addItemStackToInventory(giveStack);
			
			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(),35);
			
			return;
			
		}
		
		worldIn.setBlockState(pos.up(), this.getDefaultState().with(ISBOTTOM, false).with(FACING, state.get(FACING)).with(ISLEFT, true));
		
		if(!worldIn.isRemote())
		{
			ItemTraderTileEntity tileEntity = (ItemTraderTileEntity)worldIn.getTileEntity(pos);
			if(tileEntity != null)
			{
				tileEntity.initOwner(PlayerReference.of(player));
				if(stack.hasDisplayName())
					tileEntity.getCoreSettings().setCustomName(null, stack.getDisplayName().getString());
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
				trader.getCoreSettings().updateNames(playerEntity);
				TileEntityUtil.sendUpdatePacket(tileEntity);
				trader.openTradeMenu((ServerPlayerEntity)playerEntity);
			}
		}
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}
	
	public TileEntity getTileEntity(BlockState state, IWorld world, BlockPos pos)
	{
		
		BlockPos getPos = pos;
		if(!state.get(ISLEFT))
			getPos = getLeftPos(state, pos);
		
		if(state.get(ISBOTTOM))
			return world.getTileEntity(getPos);
		else
			return world.getTileEntity(getPos.down());
		
	}
	
	@Override
	public IItemHandlerTileEntity getItemHandlerEntity(BlockState state, World world, BlockPos pos)
	{
		TileEntity trader = this.getTileEntity(state, world, pos);
		if(trader instanceof IItemHandlerTileEntity)
			return (IItemHandlerTileEntity)trader;
		return null;
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		if(state.get(ISBOTTOM) && state.get(ISLEFT))
			return new ItemTraderTileEntity(TRADECOUNT);
		return new ItemInterfaceTileEntity();
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
			Vector3f rightOffset = MathUtil.VectorMult(right, 3.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 27f/16f);
			firstPosition = MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 1)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 10.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 27f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 2)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 17.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 27f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 3)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 24.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 27f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 4)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 3.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 20f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 5)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 10.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 20f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 6)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 17.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 20f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 7)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 24.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 20f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 8)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 3.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 13f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 9)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 10.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 13f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 10)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 17.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 13f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 11)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 24.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 13f/16f);
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
