package io.github.lightman314.lightmanscurrency.blocks.tradeinterface;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalItemTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.tradeinterface.templates.TraderInterfaceBlock;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ItemTraderInterfaceBlock extends TraderInterfaceBlock {

	public ItemTraderInterfaceBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new UniversalItemTraderInterfaceBlockEntity(pos, state);
	}

	@Override
	protected BlockEntityType<?> interfaceType() {
		return ModBlockEntities.TRADER_INTERFACE_ITEM;
	}
	
}
