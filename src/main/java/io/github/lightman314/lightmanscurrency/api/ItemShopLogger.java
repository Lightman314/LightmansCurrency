package io.github.lightman314.lightmanscurrency.api;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class ItemShopLogger extends TextLogger{
	
	public ItemShopLogger()
	{
		super("ItemShopHistory");
	}
	
	public void AddLog(Player player, ItemTradeData trade, @Nonnull CoinValue pricePaid, boolean isCreative)
	{
		
		Component creativeText = isCreative ? new TranslatableComponent("log.shoplog.creative") : new TextComponent("");
		Component playerName = new TextComponent("§a" + player.getName().getString());
		Component boughtText = new TranslatableComponent("log.shoplog." + trade.getTradeType().name().toLowerCase());
		
		//Copy/pasted from the getTooltip function that is client-side only
		MutableComponent itemName = (new TextComponent("")).append(trade.getSellItem().getHoverName()).withStyle(trade.getSellItem().getRarity().color);
		if (trade.getSellItem().hasCustomHoverName()) {
			itemName.withStyle(ChatFormatting.ITALIC);
		}
		
		Component itemText = new TranslatableComponent("log.shoplog.item.itemformat", trade.getSellItem().getCount(), itemName);
		Component cost = getCostText(trade.getCost().isFree(), pricePaid);
		if(trade.isBarter())
		{
			//Flip the sell item to the cost position
			cost = itemText;
			MutableComponent barterItemName = (new TextComponent("")).append(trade.getBarterItem().getHoverName()).withStyle(trade.getSellItem().getRarity().color);
			if (trade.getBarterItem().hasCustomHoverName()) {
				itemName.withStyle(ChatFormatting.ITALIC);
			}
			//Put the barter item in the front so that it comes out as "Player bartered BarterItem for SellItem"
			itemText = new TranslatableComponent("log.shoplog.item.itemformat", trade.getBarterItem().getCount(), barterItemName);
		}
		
		AddLog(new TranslatableComponent("log.shoplog.item.format", creativeText, playerName, boughtText, itemText, cost));
		
	}
	
}
