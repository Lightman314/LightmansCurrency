package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces;

import io.github.lightman314.lightmanscurrency.api.traders.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.ItemPositionBlockManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.ItemPositionData;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public interface IItemTraderBlock extends ITraderBlock {

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	default ItemPositionData getItemPositionData() { return this instanceof Block b ? ItemPositionBlockManager.getDataForBlock(b) : ItemPositionData.EMPTY; }
	
}
