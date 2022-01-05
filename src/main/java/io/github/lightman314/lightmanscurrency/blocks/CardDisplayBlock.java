package io.github.lightman314.lightmanscurrency.blocks;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.TraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.tileentity.ItemInterfaceTileEntity.IItemHandlerTileEntity;
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
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CardDisplayBlock extends RotatableItemBlock implements IItemTraderBlock{
	
	public static final int TRADECOUNT = 4;
	
	public CardDisplayBlock(Properties properties)
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
				trader.getCoreSettings().updateNames(playerEntity);
				TileEntityUtil.sendUpdatePacket(tileEntity);
				trader.openTradeMenu((ServerPlayerEntity)playerEntity);
			}
		}
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
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
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isBlock) {
		//Get facing
		Direction facing = this.getFacing(state);
		//Define directions for easy positional handling
		Vector3f forward = this.getForwardVect(facing);
		Vector3f right = this.getRightVect(facing);
		Vector3f up = Vector3f.YP;
		Vector3f offset = this.getOffsetVect(facing);
		
		Vector3f firstPosition = null;
		
		if(tradeSlot == 0)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 9f/16f);
			Vector3f forwardOffset = MathUtil.VectorMult(forward, 4.5f/16f);
			firstPosition = MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 1)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 9f/16f);
			Vector3f forwardOffset = MathUtil.VectorMult(forward, 4.5f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 2)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 12f/16f);
			Vector3f forwardOffset = MathUtil.VectorMult(forward, 12f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 3)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 12f/16f);
			Vector3f forwardOffset = MathUtil.VectorMult(forward, 12f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		
		List<Vector3f> posList = new ArrayList<>(3);
		if(firstPosition != null)
		{
			posList.add(firstPosition);
			for(float distance = isBlock ? 3.2f : 0.5f; distance < 4f; distance += isBlock ? 3.2f : 0.5f)
			{
				posList.add(MathUtil.VectorAdd(firstPosition, MathUtil.VectorMult(up, distance/16F)));
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
		rotation.add(Vector3f.XP.rotationDegrees(90f));
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

	@Override
	public TileEntity getTileEntity(BlockState state, IWorld world, BlockPos pos) {
		return world.getTileEntity(pos);
	}
	
	@Override
	public IItemHandlerTileEntity getItemHandlerEntity(BlockState state, World world, BlockPos pos)
	{
		TileEntity trader = this.getTileEntity(state, world, pos);
		if(trader instanceof IItemHandlerTileEntity)
			return (IItemHandlerTileEntity)trader;
		return null;
	}
	
}
