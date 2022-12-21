package io.github.lightman314.lightmanscurrency.blocks.traderblocks;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.templates.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullSupplier;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CardDisplayBlock extends TraderBlockRotatable implements IItemTraderBlock{
	
	public static final int TRADECOUNT = 4;
	
	public CardDisplayBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new ItemTraderBlockEntity(pos, state, TRADECOUNT); }
	
	@Override
	public BlockEntityType<?> traderType() { return ModBlockEntities.ITEM_TRADER.get(); }
	
	@Override @SuppressWarnings("deprecation")
	public List<BlockEntityType<?>> validTraderTypes() { return ImmutableList.of(ModBlockEntities.ITEM_TRADER.get(), ModBlockEntities.OLD_ITEM_TRADER.get()); }
	
	@Override
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade) {
		//Get facing
		Direction facing = this.getFacing(state);
		//Define directions for easy positional handling
		Vector3f forward = IRotatableBlock.getForwardVect(facing);
		Vector3f right = IRotatableBlock.getRightVect(facing);
		Vector3f up = MathUtil.getYP();
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
	public List<Quaternionf> GetStackRenderRot(int tradeSlot, BlockState state)
	{
		List<Quaternionf> rotation = new ArrayList<>();
		int facing = this.getFacing(state).get2DDataValue();
		rotation.add(MathUtil.fromAxisAngleDegree(MathUtil.getYP(), facing * -90f));
		rotation.add(MathUtil.fromAxisAngleDegree(MathUtil.getXP(), 90f));
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
	protected NonNullSupplier<List<Component>> getItemTooltips() { return LCTooltips.ITEM_TRADER; }
	
}
