package io.github.lightman314.lightmanscurrency.trader;

import java.util.List;

import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.api.ItemShopLogger;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlockEntity;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.*;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.settings.ItemTraderSettings;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData.ItemTradeType;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler.ITradeRuleMessageHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public interface IItemTrader extends ITrader, IItemHandlerBlockEntity, ITradeRuleHandler, ITradeRuleMessageHandler, ILoggerSupport<ItemShopLogger>, ITradeSource<ItemTradeData> {

	public List<ItemTradeData> getAllTrades();
	public Container getStorage();
	public void markTradesDirty();
	public void markStorageDirty();
	public void openTradeMenu(Player player);
	public void openStorageMenu(Player player);
	public void openItemEditMenu(Player player, int tradeIndex);
	public ItemTraderSettings getItemSettings();
	public void markItemSettingsDirty();
	//Open menu functions
	public ITradeRuleScreenHandler getRuleScreenHandler();
	public void sendTradeRuleUpdateMessage(int tradeIndex, ResourceLocation type, CompoundTag updateInfo);
	public void sendSetTradeItemMessage(int tradeIndex, ItemStack sellItem, int slot);
	public void sendSetTradePriceMessage(int tradeIndex, CoinValue newPrice, String newCustomName, ItemTradeType newTradeType);
	
	
	default PreTradeEvent runPreTradeEvent(Player player, int tradeIndex) { return this.runPreTradeEvent(PlayerReference.of(player), tradeIndex); }
	default PreTradeEvent runPreTradeEvent(PlayerReference player, int tradeIndex)
	{
		ItemTradeData trade = this.getTrade(tradeIndex);
		PreTradeEvent event = new PreTradeEvent(player, trade, () -> this);
		trade.beforeTrade(event);
		if(this instanceof ITradeRuleHandler)
			((ITradeRuleHandler)this).beforeTrade(event);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}
	
	default TradeCostEvent runTradeCostEvent(Player player, int tradeIndex) { return this.runTradeCostEvent(PlayerReference.of(player), tradeIndex); }
	default TradeCostEvent runTradeCostEvent(PlayerReference player, int tradeIndex)
	{
		ItemTradeData trade = this.getTrade(tradeIndex);
		TradeCostEvent event = new TradeCostEvent(player, trade, () -> this);
		trade.tradeCost(event);
		if(this instanceof ITradeRuleHandler)
			((ITradeRuleHandler)this).tradeCost(event);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}
	
	default void runPostTradeEvent(Player player, int tradeIndex, CoinValue pricePaid) { this.runPostTradeEvent(PlayerReference.of(player), tradeIndex, pricePaid); }
	default void runPostTradeEvent(PlayerReference player, int tradeIndex, CoinValue pricePaid)
	{
		ItemTradeData trade = this.getTrade(tradeIndex);
		PostTradeEvent event = new PostTradeEvent(player, trade, () -> this, pricePaid);
		trade.afterTrade(event);
		if(event.isDirty())
		{
			this.markTradesDirty();
			event.clean();
		}
		if(this instanceof ITradeRuleHandler)
		{
			((ITradeRuleHandler)this).afterTrade(event);
			if(event.isDirty())
			{
				((ITradeRuleHandler)this).markRulesDirty();
				event.clean();
			}
		}
		MinecraftForge.EVENT_BUS.post(event);
	}
	
}
