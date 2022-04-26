package io.github.lightman314.lightmanscurrency.blocks.traderblocks;

import java.util.ArrayList;
import java.util.List;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.blockentity.FreezerTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlock;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullSupplier;

public class FreezerBlock extends TraderBlockTallRotatable implements IItemTraderBlock{
	
	public static final int TRADECOUNT = 8;
	
	public static final VoxelShape SHAPE_SOUTH = box(0d,0d,3d,16d,32d,16d);
	public static final VoxelShape SHAPE_NORTH = box(0d,0d,0d,16d,32d,13d);
	public static final VoxelShape SHAPE_EAST = box(3d,0d,0d,16d,32d,16d);
	public static final VoxelShape SHAPE_WEST = box(0d,0d,0d,13d,32d,16d);
	
	public FreezerBlock(Properties properties)
	{
		super(properties, LazyShapes.lazyTallDirectionalShape(SHAPE_NORTH, SHAPE_EAST, SHAPE_SOUTH, SHAPE_WEST));
	}
	
	@Override
	public BlockEntity makeTrader(BlockPos pos, BlockState state) { return new FreezerTraderBlockEntity(pos, state, TRADECOUNT); }
	
	@Override
	protected BlockEntity makeDummy(BlockPos pos, BlockState state) { return new ItemInterfaceBlockEntity(pos, state); }
	
	@Override
	public BlockEntityType<?> traderType() { return ModBlockEntities.FREEZER_TRADER; }
	
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
			Vector3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 28f/16f);
			firstPosition = MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 1)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 28f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 2)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 21f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 3)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 21f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 4)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 14f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 5)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 14f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 6)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 5f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 7f/16f);
			firstPosition =  MathUtil.VectorAdd(offset, forwardOffset, rightOffset, vertOffset);
		}
		else if(tradeSlot == 7)
		{
			Vector3f rightOffset = MathUtil.VectorMult(right, 11f/16f);
			Vector3f vertOffset = MathUtil.VectorMult(up, 7f/16f);
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
	public float GetStackRenderScale(int tradeSlot, BlockState state, boolean isBlock){ return 0.4f; }
	
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
	
	@Override
	protected NonNullSupplier<List<Component>> getItemTooltips() { return LCTooltips.ITEM_TRADER; }
	
}
