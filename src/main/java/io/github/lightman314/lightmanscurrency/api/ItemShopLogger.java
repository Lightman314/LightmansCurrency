package io.github.lightman314.lightmanscurrency.api;

import io.github.lightman314.lightmanscurrency.ItemTradeData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ItemShopLogger extends TextLogger{
	
	public ItemShopLogger()
	{
		super("ItemShopHistory");
	}
	
	public void AddLog(PlayerEntity player, ItemTradeData trade, boolean isCreative)
	{
		
		ITextComponent creativeText = isCreative ? new TranslationTextComponent("log.shoplog.creative") : new StringTextComponent("");
		String playerName = player.getName().getString();
		ITextComponent boughtText = new TranslationTextComponent("log.shoplog." + trade.getTradeDirection().name().toLowerCase());
		ITextComponent itemText = new TranslationTextComponent("log.shoplog.item.itemformat", trade.getSellItem().getCount(), new TranslationTextComponent(trade.getSellItem().getTranslationKey()));
		String cost = trade.getCost().getString();
		
		AddLog(new TranslationTextComponent("log.shoplog.item.format", creativeText, playerName, boughtText, itemText, cost));
		
	}
	
}
