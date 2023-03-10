package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullSupplier;

public class CardDisplayBlock extends TraderBlockRotatable implements IItemTraderBlock {
	
	public static final int TRADECOUNT = 4;
	
	public CardDisplayBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected TileEntity makeTrader() { return new ItemTraderBlockEntity(TRADECOUNT); }

	@Override
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade) {
		//Get facing
		Direction facing = this.getFacing(state);
		//Define directions for easy positional handling
		Vector3f forward = IRotatableBlock.getForwardVect(facing);
		Vector3f right = IRotatableBlock.getRightVect(facing);
		Vector3f up = Vector3f.YP;
		Vector3f offset = IRotatableBlock.getOffsetVect(facing);
		
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
			for(float distance = 3.2f; distance < 4f; distance += 3.2f)
				posList.add(MathUtil.VectorAdd(firstPosition, MathUtil.VectorMult(up, distance/16F)));
		}
		else
		{
			posList.add(new Vector3f(0F, 1f, 0F));
		}
		return posList;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state)
	{
		List<Quaternion> rotation = new ArrayList<>();
		int facing = this.getFacing(state).get2DDataValue();
		rotation.add(Vector3f.YP.rotationDegrees(facing * -90f));
		rotation.add(Vector3f.XP.rotationDegrees(90f));
		return rotation;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public float GetStackRenderScale(int tradeSlot, BlockState state){ return 0.4f; }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex()
	{
		return TRADECOUNT;
	}
	
	@Override
	protected NonNullSupplier<List<ITextComponent>> getItemTooltips() { return LCTooltips.ITEM_TRADER; }
	
}