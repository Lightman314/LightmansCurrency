package io.github.lightman314.lightmanscurrency.blocks;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
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
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ShelfBlock extends RotatableBlock implements IItemTraderBlock{
	
	public static final int TRADECOUNT = 1;
	
	private static final VoxelShape SHAPE_NORTH = makeCuboidShape(0d,0d,0d,16d,16d,5d);
	private static final VoxelShape SHAPE_SOUTH = makeCuboidShape(0d,0d,11d,16d,16d,16d);
	private static final VoxelShape SHAPE_EAST = makeCuboidShape(11d,0d,0d,16d,16d,16d);
	private static final VoxelShape SHAPE_WEST = makeCuboidShape(0d,0d,0d,5d,16d,16d);
	
	public ShelfBlock(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new ItemTraderTileEntity(TRADECOUNT);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult result)
	{
		if(!world.isRemote())
		{
			//Open UI
			TileEntity tileEntity = world.getTileEntity(pos);
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
	
	/*@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos posIn, Random randomIn) {
        
	}*/
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
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
		
		super.onBlockHarvested(worldIn, pos, state, player);
		
	}

	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isBlock) {
		List<Vector3f> posList = new ArrayList<Vector3f>(1);
		Direction facing = this.getFacing(state);
		//Define directions for easy positional handling
		Vector3f forward = this.getForwardVect(facing);
		Vector3f right = this.getRightVect(facing);
		Vector3f up = Vector3f.YP;
		Vector3f offset = this.getOffsetVect(facing);
		
		if(tradeSlot == 0)
		{
			Vector3f firstPosition = MathUtil.VectorAdd(offset, MathUtil.VectorMult(right, 0.5f), MathUtil.VectorMult(forward, 14.5f/16f), MathUtil.VectorMult(up, 9f/16f));
			posList.add(firstPosition); 
			for(float distance = isBlock ? -3.2f : -1f; distance >= -3f; distance -= isBlock ? 3.2f : 1f)
			{
				posList.add(MathUtil.VectorAdd(firstPosition, MathUtil.VectorMult(forward, distance/16f)));
			}
		}
		
		return posList;
	}
	
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state, boolean isBlock)
	{
		//Return null for automatic rotation
		List<Quaternion> rotation = new ArrayList<>();
		int facing = state.get(FACING).getHorizontalIndex();
		rotation.add(Vector3f.YP.rotationDegrees(facing * -90f));
		return rotation;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Vector3f GetStackRenderScale(int tradeSlot, BlockState state, boolean isBlock){
		return new Vector3f(14f/16f, 14f/16f, 14f/16f);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex()
	{
		return TRADECOUNT;
	}

	@Override
	public TileEntity getTileEntity(BlockState state, IWorld world, BlockPos pos) {
		return world.getTileEntity(pos);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext contect)
	{
		if(state.get(FACING) == Direction.NORTH)
			return SHAPE_NORTH;
		else if(state.get(FACING) == Direction.SOUTH)
			return SHAPE_SOUTH;
		else if(state.get(FACING) == Direction.EAST)
			return SHAPE_EAST;
		return SHAPE_WEST;
	}
	
}
