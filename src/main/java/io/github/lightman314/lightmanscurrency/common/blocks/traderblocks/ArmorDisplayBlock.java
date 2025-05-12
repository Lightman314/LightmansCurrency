package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ArmorDisplayTraderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class ArmorDisplayBlock extends TraderBlockTallRotatable implements IItemTraderBlock, IVariantBlock {
	
	public ArmorDisplayBlock(Properties properties) { super(properties); }

	@Override
	protected boolean isBlockOpaque() { return false; }

	@Override
	public BlockEntity makeTrader(BlockPos pos, BlockState state) {
		ArmorDisplayTraderBlockEntity trader = new ArmorDisplayTraderBlockEntity(pos, state);
		trader.flagAsLoaded();
		return trader;
	}
	
	@Override
	public BlockEntityType<?> traderType() { return ModBlockEntities.ARMOR_TRADER.get(); }
	
	@Override
	public void onRemove(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.is(newState.getBlock()))
		{
			super.onRemove(state,level,pos,newState,isMoving);
			return;
		}
		if(level.getBlockEntity(pos) instanceof ArmorDisplayTraderBlockEntity be)
			be.destroyArmorStand();
		super.onRemove(state, level, pos, newState, isMoving);
	}
	
	@Override
	protected Supplier<List<Component>> getItemTooltips() { return LCText.TOOLTIP_ITEM_TRADER_ARMOR.asTooltip(ArmorDisplayTraderBlockEntity.TRADE_COUNT); }
	
}
