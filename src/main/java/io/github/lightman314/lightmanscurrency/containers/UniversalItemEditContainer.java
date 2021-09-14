package io.github.lightman314.lightmanscurrency.containers;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.containers.interfaces.IItemTrader;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import net.minecraft.entity.player.PlayerInventory;

public class UniversalItemEditContainer extends ItemEditContainer{
	
	public UniversalItemEditContainer(int windowId, PlayerInventory inventory, Supplier<IItemTrader> traderSource, int tradeIndex)
	{
		super(ModContainers.UNIVERSAL_ITEM_EDIT, windowId, inventory, traderSource, tradeIndex, traderSource.get().getTrade(tradeIndex));
	}
	
}
