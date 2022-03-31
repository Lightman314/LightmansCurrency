package io.github.lightman314.lightmanscurrency.api;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemShopLogger extends TextLogger{
	
	public ItemShopLogger()
	{
		super("ItemShopHistory");
	}
	
	/**
	 * @deprecated Use AddLog(PlayerReference,ItemTradeData,CoinValue,boolean) instead
	 */
	@Deprecated
	public void AddLog(Player player, ItemTradeData trade, @Nonnull CoinValue pricePaid, boolean isCreative) {
		this.AddLog(PlayerReference.of(player), trade, pricePaid, isCreative);
	}
	
	public void AddLog(PlayerReference player, ItemTradeData trade, @Nonnull CoinValue pricePaid, boolean isCreative)
	{
		
		Component creativeText = isCreative ? new TranslatableComponent("log.shoplog.creative") : new TextComponent("");
		Component playerName = new TextComponent("§a" + player.lastKnownName());
		Component boughtText = new TranslatableComponent("log.shoplog." + trade.getTradeType().name().toLowerCase());
		
		Component itemText = trade.isPurchase() ? this.getItemInputComponent(trade.getSellItem(0), trade.getSellItem(1)) : this.getItemOutputComponent(trade.getSellItem(0), trade.getCustomName(0), trade.getSellItem(1), trade.getCustomName(1));
		Component cost = getCostText(pricePaid);
		if(trade.isBarter())
		{
			//Flip the sell item to the cost position
			cost = itemText;
			//Put the barter item in the front so that it comes out as "Player bartered BarterItem for SellItem"
			itemText = getItemInputComponent(trade.getBarterItem(0), trade.getBarterItem(1));
		}
		
		AddLog(new TranslatableComponent("log.shoplog.item.format", creativeText, playerName, boughtText, itemText, cost));
		
	}
	
	private Component getItemInputComponent(ItemStack item1, ItemStack item2) {
		return getItemOutputComponent(item1, "", item2, "");
	}
	
	private Component getItemOutputComponent(ItemStack item1, String customName1, ItemStack item2, String customName2) {
		if(item1.isEmpty() && item2.isEmpty())
			return new TextComponent("");
		if(item1.isEmpty() && !item2.isEmpty())
		{
			return getItemComponent(item2, customName2);
		}
		else if(!item1.isEmpty() && item2.isEmpty())
		{
			return getItemComponent(item1, customName1);
		}
		else
		{
			return new TranslatableComponent("log.shoplog.and", getItemComponent(item1, customName1), getItemComponent(item2, customName2));
		}
	}
	
	private Component getItemComponent(ItemStack item, String customName) {
		//Copy/pasted from the getTooltip function that is client-side only
		MutableComponent itemName = (new TextComponent("")).append(customName.isBlank() ? item.getHoverName() : new TextComponent(customName)).withStyle(item.getRarity().color);
		if (item.hasCustomHoverName() && customName.isBlank()) {
			itemName.withStyle(ChatFormatting.ITALIC);
		}
		return new TranslatableComponent("log.shoplog.item.itemformat", item.getCount(), itemName);
	}
	
	
}
