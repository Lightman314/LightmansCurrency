package io.github.lightman314.lightmanscurrency.api;

import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData;
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
	
	public void AddLog(PlayerEntity player, ItemTradeData trade, boolean isCreative)
	{
		
		ITextComponent creativeText = isCreative ? new TranslationTextComponent("log.shoplog.creative") : new StringTextComponent("");
		ITextComponent playerName = new StringTextComponent("§a" + player.getName().getString());
		ITextComponent boughtText = new TranslationTextComponent("log.shoplog." + trade.getTradeDirection().name().toLowerCase());
		
		//Copy/pasted from the getTooltip function that is client-side only
		IFormattableTextComponent itemName = (new StringTextComponent("")).append(trade.getSellItem().getDisplayName()).mergeStyle(trade.getSellItem().getRarity().color);
		if (trade.getSellItem().hasDisplayName()) {
			itemName.mergeStyle(TextFormatting.ITALIC);
		}
		
		ITextComponent itemText = new TranslationTextComponent("log.shoplog.item.itemformat", trade.getSellItem().getCount(), itemName);
		ITextComponent cost = new StringTextComponent("§e" + trade.getCost().getString());
		
		AddLog(new TranslationTextComponent("log.shoplog.item.format", creativeText, playerName, boughtText, itemText, cost));
		
	}
	
}
