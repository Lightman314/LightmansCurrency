package io.github.lightman314.lightmanscurrency.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.blockentity.DummyBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.FreezerTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.TickableBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.blocks.util.TickerUtil;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FreezerBlock extends TallRotatableTraderBlock{
	
	public static final int TRADECOUNT = 8;
	
	private static final VoxelShape SHAPE_SOUTH = box(0d,0d,3d,16d,32d,16d);
	private static final VoxelShape SHAPE_NORTH = box(0d,0d,0d,16d,32d,13d);
	private static final VoxelShape SHAPE_EAST = box(3d,0d,0d,16d,32d,16d);
	private static final VoxelShape SHAPE_WEST = box(0d,0d,0d,13d,32d,16d);
	protected static final BooleanProperty ISBOTTOM = BlockStateProperties.BOTTOM;
	private static final BiFunction<Direction, Boolean, VoxelShape> LAZY_SHAPE = LazyShapes.lazyTallDirectionalShape(SHAPE_NORTH, SHAPE_EAST, SHAPE_SOUTH, SHAPE_WEST);
	
	public FreezerBlock(Properties properties)
	{
		super(properties, TRADECOUNT);
		//this.flagAsTransparent();
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return TickerUtil.createTickerHelper(type, ModBlockEntities.FREEZER_TRADER, TickableBlockEntity::tickHandler);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		if(state.getValue(ISBOTTOM))
			return new FreezerTraderBlockEntity(pos, state, TRADECOUNT);
		return new DummyBlockEntity(pos, state);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return LAZY_SHAPE.apply(state.getValue(FACING), state.getValue(ISBOTTOM));
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
		int facing = MathUtil.getHorizontalFacing(state.getValue(FACING));
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
