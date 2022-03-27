package io.github.lightman314.lightmanscurrency.menus;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.trader.ITrader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class TraderStorageMenu extends AbstractContainerMenu {

	
	Container interactionOutputs = new SimpleContainer(1);
	
	public TraderStorageMenu(int windowID, Inventory inventory, BlockPos traderPos) {
		this(null, windowID, inventory, null);
	}
	
	protected TraderStorageMenu(MenuType<?> type, int windowID, Inventory inventory, Supplier<ITrader> trader) {
		super(type, windowID);
		
		
	}

	@Override
	public boolean stillValid(Player p_38874_) {
		return false;
	}
	
	
	
}
