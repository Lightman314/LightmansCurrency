package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockBase;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullSupplier;

public class DisplayCaseBlock extends TraderBlockBase implements IItemTraderBlock {
	
	public static final int TRADECOUNT = 1;
	
	public DisplayCaseBlock(Properties properties)
	{
		super(properties);
	}
	
	@Override
	protected TileEntity makeTrader() { return new ItemTraderBlockEntity(TRADECOUNT); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade) {
		List<Vector3f> posList = new ArrayList<>(1);
		posList.add(new Vector3f(0.5F, 0.5F + 2F/16F, 0.5F));
		return posList;
	}
	
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state)
	{
		//Return null for automatic rotation
		return null;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public float GetStackRenderScale(int tradeSlot, BlockState state){ return 0.75f; }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex()
	{
		return TRADECOUNT;
	}
	
	@Override
	protected NonNullSupplier<List<ITextComponent>> getItemTooltips() { return LCTooltips.ITEM_TRADER; }
	
}