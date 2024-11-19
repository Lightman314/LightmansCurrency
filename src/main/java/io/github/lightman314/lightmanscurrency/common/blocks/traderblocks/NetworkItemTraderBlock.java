package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.QuarantineAPI;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockRotatable;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class NetworkItemTraderBlock extends TraderBlockRotatable {

	public static final int TRADER_COUNT_SMALL = 4;
	public static final int TRADER_COUNT_MEDIUM = 8;
	public static final int TRADER_COUNT_LARGE = 12;
	public static final int TRADER_COUNT_XLARGE = 16;
	
	private final int tradeCount;
	
	public NetworkItemTraderBlock(Properties properties, int tradeCount) { super(properties); this.tradeCount = tradeCount; }

	@Override
	protected boolean isBlockOpaque() { return false; }

	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new ItemTraderBlockEntity(pos, state, this.tradeCount, true); }

	@Override
	protected BlockEntityType<?> traderType() { return ModBlockEntities.ITEM_TRADER.get(); }
	
	@Override
	protected List<BlockEntityType<?>> validTraderTypes() { return ImmutableList.of(ModBlockEntities.ITEM_TRADER.get()); }

	@Override
	protected Supplier<List<Component>> getItemTooltips() { return LCText.TOOLTIP_ITEM_TRADER_NETWORK.asTooltip(this.tradeCount); }

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		if(QuarantineAPI.IsDimensionQuarantined(context))
			tooltip.add(LCText.TOOLTIP_DIMENSION_QUARANTINED_NETWORK_TRADER.getWithStyle(ChatFormatting.GOLD));
	}
}
