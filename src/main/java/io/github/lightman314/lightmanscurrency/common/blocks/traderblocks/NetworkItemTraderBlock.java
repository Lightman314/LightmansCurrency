package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

public class NetworkItemTraderBlock extends TraderBlockRotatable {

	public static final int TRADER_COUNT_SMALL = 4;
	public static final int TRADER_COUNT_MEDIUM = 8;
	public static final int TRADER_COUNT_LARGE = 12;
	public static final int TRADER_COUNT_XLARGE = 16;
	
	private final int tradeCount;
	
	public NetworkItemTraderBlock(Properties properties, int tradeCount) { super(properties); this.tradeCount = tradeCount; }

	@Override
	protected TileEntity makeTrader() { return new ItemTraderBlockEntity(this.tradeCount, true); }

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCTooltips.ITEM_NETWORK_TRADER);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
}