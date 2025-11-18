package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockBase;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DisplayCaseBlock extends TraderBlockBase implements IItemTraderBlock, IVariantBlock {
	
	public static final int TRADECOUNT = 1;

	private final Color color;

	public DisplayCaseBlock(Properties properties, Color color) { super(properties); this.color = color; }

	@Override
	public String getDescriptionId() {
		if(this.color == Color.WHITE)
			return super.getDescriptionId();
		return ModBlocks.DISPLAY_CASE.get(Color.WHITE).getDescriptionId();
	}

	@Override
	protected boolean isBlockOpaque() { return false; }

	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new ItemTraderBlockEntity(pos, state, TRADECOUNT); }
	
	@Override
	public BlockEntityType<?> traderType() { return ModBlockEntities.ITEM_TRADER.get(); }
	
	@Override
	public List<BlockEntityType<?>> validTraderTypes() { return ImmutableList.of(ModBlockEntities.ITEM_TRADER.get()); }
	
	@Override
	protected Supplier<List<Component>> getItemTooltips() { return LCText.TOOLTIP_ITEM_TRADER.asTooltip(TRADECOUNT); }

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(LCText.TOOLTIP_COLORED_ITEM.get(this.color.getComponent()));
		super.appendHoverText(stack, context, tooltip, flagIn);
	}

}
