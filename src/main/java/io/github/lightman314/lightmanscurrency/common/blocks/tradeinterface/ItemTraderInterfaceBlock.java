package io.github.lightman314.lightmanscurrency.common.blocks.tradeinterface;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.blockentity.ItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blocks.TraderInterfaceBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ItemTraderInterfaceBlock extends TraderInterfaceBlock {

	public ItemTraderInterfaceBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ItemTraderInterfaceBlockEntity(pos, state);
	}

	@Override
	protected BlockEntityType<?> interfaceType() {
		return ModBlockEntities.TRADER_INTERFACE_ITEM.get();
	}
	
	@Override
	protected Supplier<List<Component>> getItemTooltips() { return LCText.TOOLTIP_INTERFACE_ITEM.asTooltip(); }

	@Override
	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderInterfaceBlockEntity trader) { }
	
}
