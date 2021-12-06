package io.github.lightman314.lightmanscurrency.containers;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import net.minecraft.world.entity.player.Inventory;

public class UniversalItemEditContainer extends ItemEditContainer{
	
	public UniversalItemEditContainer(int windowId, Inventory inventory, Supplier<IItemTrader> traderSource, int tradeIndex)
	{
		super(ModContainers.UNIVERSAL_ITEM_EDIT, windowId, inventory, traderSource, tradeIndex, traderSource.get().getTrade(tradeIndex));
	}
	
}
