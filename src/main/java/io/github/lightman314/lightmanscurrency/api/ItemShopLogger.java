package io.github.lightman314.lightmanscurrency.api;

import io.github.lightman314.lightmanscurrency.ItemTradeData;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class ItemShopLogger extends TextLogger{
	
	public ItemShopLogger()
	{
		super("ItemShopHistory");
	}
	
	public void AddLog(Player player, ItemTradeData trade, boolean isCreative)
	{
		
		String playerName = player.getName().getString();
		MutableComponent boughtText = new TranslatableComponent("log.shoplog." + trade.getTradeDirection().name().toLowerCase());
		MutableComponent itemText = new TranslatableComponent("log.shoplog.item.itemformat", trade.getSellItem().getCount(), trade.getSellItem().getDisplayName());
		String cost = trade.getCost().getString();
		MutableComponent creativeText = isCreative ? new TranslatableComponent("log.shoplog.creative") : new TextComponent("");
		
		AddLog(new TranslatableComponent("log.shoplog.item.format", creativeText, playerName, boughtText, itemText, cost));
		
	}
	
}
