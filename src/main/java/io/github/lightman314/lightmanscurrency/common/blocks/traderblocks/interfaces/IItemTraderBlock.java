package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.interfaces;

import io.github.lightman314.lightmanscurrency.api.traders.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionBlockManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionData;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public interface IItemTraderBlock extends ITraderBlock {

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	default ItemPositionData getItemPositionData() { return this instanceof Block b ? ItemPositionBlockManager.getDataForBlock(b) : ItemPositionData.EMPTY; }
	
}
