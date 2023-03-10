package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullSupplier;

public class ShelfBlock extends TraderBlockRotatable implements IItemTraderBlock{
	
	public static final int TRADECOUNT = 1;
	
	private static final VoxelShape SHAPE_NORTH = box(0d,0d,0d,16d,16d,5d);
	private static final VoxelShape SHAPE_SOUTH = box(0d,0d,11d,16d,16d,16d);
	private static final VoxelShape SHAPE_EAST = box(11d,0d,0d,16d,16d,16d);
	private static final VoxelShape SHAPE_WEST = box(0d,0d,0d,5d,16d,16d);
	
	public ShelfBlock(Properties properties)
	{
		super(properties, LazyShapes.lazyDirectionalShape(SHAPE_NORTH, SHAPE_EAST, SHAPE_SOUTH, SHAPE_WEST));
	}

	@Override
	public TileEntity makeTrader() { return new ItemTraderBlockEntity(TRADECOUNT); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade) {
		List<Vector3f> posList = new ArrayList<Vector3f>(1);
		if(tradeSlot == 0)
		{
			Direction facing = this.getFacing(state);
			//Define directions for easy positional handling
			Vector3f forward = IRotatableBlock.getForwardVect(facing);
			Vector3f right = IRotatableBlock.getRightVect(facing);
			Vector3f up = Vector3f.YP;
			Vector3f offset = IRotatableBlock.getOffsetVect(facing);
			//Only 1 position for shelves
			posList.add(MathUtil.VectorAdd(offset, MathUtil.VectorMult(right, 0.5f), MathUtil.VectorMult(forward, 14.5f/16f), MathUtil.VectorMult(up, 9f/16f)));
		}
		
		return posList;
	}
	
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state)
	{
		//Return null for automatic rotation
		List<Quaternion> rotation = new ArrayList<>();
		int facing = this.getFacing(state).get2DDataValue();
		rotation.add(Vector3f.YP.rotationDegrees(facing * -90f));
		return rotation;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public float GetStackRenderScale(int tradeSlot, BlockState state){ return 14f/16f; }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex()
	{
		return TRADECOUNT;
	}
	
	@Override
	protected NonNullSupplier<List<ITextComponent>> getItemTooltips() { return LCTooltips.ITEM_TRADER; }
	
}