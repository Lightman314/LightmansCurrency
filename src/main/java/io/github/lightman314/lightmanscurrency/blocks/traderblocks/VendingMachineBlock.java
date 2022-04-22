package io.github.lightman314.lightmanscurrency.blocks.traderblocks;

import java.util.ArrayList;
import java.util.List;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlock;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VendingMachineBlock extends TraderBlockTallRotatable implements IItemTraderBlock{
	
	public static final int TRADECOUNT = 6;
	
	public VendingMachineBlock(Properties properties)
	{
		super(properties);
		
	}
	
	@Override
	public BlockEntity makeTrader(BlockPos pos, BlockState state) { return new ItemTraderBlockEntity(pos, state, TRADECOUNT); }
	
	@Override
	protected BlockEntity makeDummy(BlockPos pos, BlockState state) { return new ItemInterfaceBlockEntity(pos, state); }
	
	@Override
	public BlockEntityType<?> traderType() { return ModBlockEntities.ITEM_TRADER; }
	
	@Override
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isBlock, boolean isDoubleTrade) {
		//Get facing
		Direction facing = this.getFacing(state);
		//Define directions for easy positional handling
		Vector3f forward = IRotatableBlock.getForwardVect(facing);
		Vector3f right = IRotatableBlock.getRightVect(facing);
		Vector3f up = Vector3f.YP;
		Vector3f offset = IRotatableBlock.getOffsetVect(facing);
		
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
			Vector3f rightOffset = MathUtil.VectorMult(right, 9.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 27f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 2)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 3.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 20f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 3)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 9.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 20f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 4)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 3.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 13f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 5)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 9.5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 13f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		
		List<Vector3f> posList = new ArrayList<>(3);
		if(firstPosition != null)
		{
			posList.add(firstPosition);
			float deltaDist = isBlock ? (isDoubleTrade ? 1.6f : 3.2f) : 0.5f;
			for(float distance = deltaDist; distance < 7; distance += deltaDist)
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
		int facing = this.getFacing(state).get2DDataValue();
		rotation.add(Vector3f.YP.rotationDegrees(facing * -90f));
		return rotation;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public float GetStackRenderScale(int tradeSlot, BlockState state, boolean isBlock){ return 0.3f; }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex()
	{
		return TRADECOUNT;
	}
	
	@Override
	public Direction getRelativeSide(BlockState state, Direction side) {
		return IItemHandlerBlock.getRelativeSide(this.getFacing(state), side);
	}

	@Override
	public IItemHandlerBlockEntity getItemHandlerEntity(BlockState state, Level level, BlockPos pos) {
		BlockEntity blockEntity = this.getBlockEntity(state, level, pos);
		if(blockEntity instanceof IItemHandlerBlockEntity)
			return (IItemHandlerBlockEntity)blockEntity;
		return null;
	}
	
}
