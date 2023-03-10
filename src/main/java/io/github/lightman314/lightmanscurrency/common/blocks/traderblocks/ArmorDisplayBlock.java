package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ArmorDisplayTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class ArmorDisplayBlock extends TraderBlockTallRotatable implements IItemTraderBlock {
	
	public ArmorDisplayBlock(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public TileEntity makeTrader() {
		ArmorDisplayTraderBlockEntity trader = new ArmorDisplayTraderBlockEntity();
		trader.flagAsLoaded();
		return trader;
	}
	
	@Override
	public void onRemove(BlockState state, @Nonnull World level, @Nonnull BlockPos pos, BlockState newState, boolean isMoving) {
		TileEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof ArmorDisplayTraderBlockEntity)
		{
			ArmorDisplayTraderBlockEntity trader = (ArmorDisplayTraderBlockEntity)blockEntity;
			trader.destroyArmorStand();
		}

		super.onRemove(state, level, pos, newState, isMoving);
	}
	
	@Override
	public List<Vector3f> GetStackRenderPos(int tradeSlot, BlockState state, boolean isDoubleTrade) { return new ArrayList<>(); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, BlockState state) { return new ArrayList<>(); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public float GetStackRenderScale(int tradeSlot, BlockState state){ return 0f; }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex() { return -1; }
	
	@Override
	protected NonNullSupplier<List<ITextComponent>> getItemTooltips() { return LCTooltips.ITEM_TRADER_ARMOR; }
	
}
