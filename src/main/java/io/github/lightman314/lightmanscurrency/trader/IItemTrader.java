package io.github.lightman314.lightmanscurrency.trader;

import java.util.List;

import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;

public interface IItemTrader extends ITrader {

	public ItemTradeData getTrade(int index);
	public List<ItemTradeData> getAllTrades();
	public IInventory getStorage();
	public void markTradesDirty();
	public void openTradeMenu(PlayerEntity player);
	public void openStorageMenu(PlayerEntity player);
	public void openItemEditMenu(PlayerEntity player, int tradeIndex);
	
}