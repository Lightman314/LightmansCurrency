package io.github.lightman314.lightmanscurrency.api;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ItemShopLogger extends TextLogger{
	
	public ItemShopLogger()
	{
		super("ItemShopHistory");
	}
	
	public void AddLog(PlayerEntity player, ItemTradeData trade, @Nonnull CoinValue pricePaid, boolean isCreative) {
		this.AddLog(PlayerReference.of(player), trade, pricePaid, isCreative);
	}
	
	public void AddLog(PlayerReference player, ItemTradeData trade, @Nonnull CoinValue pricePaid, boolean isCreative)
	{
		
		ITextComponent creativeText = isCreative ? new TranslationTextComponent("log.shoplog.creative") : new StringTextComponent("");
		ITextComponent playerName = new StringTextComponent("§a" + player.lastKnownName());
		ITextComponent boughtText = new TranslationTextComponent("log.shoplog." + trade.getTradeType().name().toLowerCase());
		
		//Copy/pasted from the getTooltip function that is client-side only
		IFormattableTextComponent itemName = (new StringTextComponent("")).append(trade.getSellItem().getDisplayName()).mergeStyle(trade.getSellItem().getRarity().color);
		if (trade.getSellItem().hasDisplayName()) {
			itemName.mergeStyle(TextFormatting.ITALIC);
		}
		
		ITextComponent itemText = new TranslationTextComponent("log.shoplog.item.itemformat", trade.getSellItem().getCount(), itemName);
		ITextComponent cost = getCostText(pricePaid);
		if(trade.isBarter())
		{
			//Flip the sell item to the cost position
			cost = itemText;
			IFormattableTextComponent barterItemName = (new StringTextComponent("")).append(trade.getBarterItem().getDisplayName()).mergeStyle(trade.getSellItem().getRarity().color);
			if (trade.getBarterItem().hasDisplayName()) {
				itemName.mergeStyle(TextFormatting.ITALIC);
			}
			//Put the barter item in the front so that it comes out as "Player bartered BarterItem for SellItem"
			itemText = new TranslationTextComponent("log.shoplog.item.itemformat", trade.getBarterItem().getCount(), barterItemName);
		}
		
		AddLog(new TranslationTextComponent("log.shoplog.item.format", creativeText, playerName, boughtText, itemText, cost));
		
	}
	
}
