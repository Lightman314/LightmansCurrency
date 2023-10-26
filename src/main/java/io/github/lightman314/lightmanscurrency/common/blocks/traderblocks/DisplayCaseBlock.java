package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.List;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates.TraderBlockBase;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.NonNullSupplier;

public class DisplayCaseBlock extends TraderBlockBase implements IItemTraderBlock {
	
	public static final int TRADECOUNT = 1;
	
	public DisplayCaseBlock(Properties properties) { super(properties); }
	
	@Override
	protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new ItemTraderBlockEntity(pos, state, TRADECOUNT); }
	
	@Override
	public BlockEntityType<?> traderType() { return ModBlockEntities.ITEM_TRADER.get(); }
	
	@Override
	public List<BlockEntityType<?>> validTraderTypes() { return ImmutableList.of(ModBlockEntities.ITEM_TRADER.get()); }

	@Override
	protected NonNullSupplier<List<Component>> getItemTooltips() { return LCTooltips.ITEM_TRADER; }

	@Override
	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderData trader) { }
	
}
