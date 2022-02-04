package io.github.lightman314.lightmanscurrency.menus;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.core.ModMenus;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import net.minecraft.world.entity.player.Inventory;

public class UniversalItemEditMenu extends ItemEditMenu{
	
	public UniversalItemEditMenu(int windowId, Inventory inventory, Supplier<IItemTrader> traderSource, int tradeIndex)
	{
		super(ModMenus.UNIVERSAL_ITEM_EDIT, windowId, inventory, traderSource, tradeIndex, traderSource.get().getTrade(tradeIndex));
	}
	
}
