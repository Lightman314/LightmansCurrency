package io.github.lightman314.lightmanscurrency.containers.interfaces;

import io.github.lightman314.lightmanscurrency.ItemTradeData;
import net.minecraft.world.entity.player.Player;

public interface IItemTrader {

	public ItemTradeData getTrade(int index);
	public int getTradeCount();
	public void markTradesDirty();
	public void markLoggerDirty();
	public void openTradeMenu(Player player);
	public void openStorageMenu(Player player);
	public void openItemEditMenu(Player player, int tradeIndex);
	
}
