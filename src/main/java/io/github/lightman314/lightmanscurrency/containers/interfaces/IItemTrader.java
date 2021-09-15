package io.github.lightman314.lightmanscurrency.containers.interfaces;

import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData;
import net.minecraft.entity.player.PlayerEntity;

public interface IItemTrader {

	public ItemTradeData getTrade(int index);
	public int getTradeCount();
	public void markTradesDirty();
	public void openTradeMenu(PlayerEntity player);
	public void openStorageMenu(PlayerEntity player);
	public void openItemEditMenu(PlayerEntity player, int tradeIndex);
	
}
