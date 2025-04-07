package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class VendingMachineBlock extends TraderBlockTallRotatable implements IItemTraderBlock {
	
	public static final int TRADECOUNT = 6;
	public static final ResourceLocation TYPE = VersionUtil.lcResource("vending_machine");

	public VendingMachineBlock(Properties properties) { super(properties); }

	@Override
	protected boolean isBlockOpaque() { return false; }

	@Override
	public BlockEntity makeTrader(BlockPos pos, BlockState state) { return new ItemTraderBlockEntity(pos, state, TRADECOUNT); }
	
	@Override
	public BlockEntityType<?> traderType() { return ModBlockEntities.ITEM_TRADER.get(); }
	
	@Override
	protected Supplier<List<Component>> getItemTooltips() { return LCText.TOOLTIP_ITEM_TRADER.asTooltip(TRADECOUNT); }

}
