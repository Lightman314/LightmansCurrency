package io.github.lightman314.lightmanscurrency.trader;

import java.util.List;

import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public interface IItemTrader extends ITrader {

	public ItemTradeData getTrade(int index);
	public List<ItemTradeData> getAllTrades();
	public Container getStorage();
	public void markTradesDirty();
	public void openTradeMenu(Player player);
	public void openStorageMenu(Player player);
	public void openItemEditMenu(Player player, int tradeIndex);
	
}
