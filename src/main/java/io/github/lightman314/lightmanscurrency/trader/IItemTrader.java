package io.github.lightman314.lightmanscurrency.trader;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.api.ItemShopLogger;
import io.github.lightman314.lightmanscurrency.blockentity.ItemInterfaceBlockEntity.IItemHandlerBlockEntity;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.item.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.menus.traderstorage.item.ItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.trader.common.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.trader.settings.ItemTraderSettings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler.ITradeRuleMessageHandler;
import io.github.lightman314.lightmanscurrency.upgrades.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType.IUpgradeable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface IItemTrader extends ITrader, IUpgradeable, IItemHandlerBlockEntity, ITradeRuleHandler, ITradeRuleMessageHandler, ILoggerSupport<ItemShopLogger>, ITradeSource<ItemTradeData> {

	public static final int DEFAULT_STACK_LIMIT = 64 * 9;
	
	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(UpgradeType.ITEM_CAPACITY);
	
	public default boolean allowUpgrade(UpgradeType type) {
		return ALLOWED_UPGRADES.contains(type);
	}
	
	public List<ItemTradeData> getAllTrades();
	public default int getStorageStackLimit() {
		int limit = DEFAULT_STACK_LIMIT;
		for(int i = 0; i < this.getUpgradeInventory().getContainerSize(); ++i)
		{
			ItemStack stack = this.getUpgradeInventory().getItem(i);
			if(stack.getItem() instanceof UpgradeItem)
			{
				UpgradeItem upgradeItem = (UpgradeItem)stack.getItem();
				if(this.allowUpgrade(upgradeItem))
				{
					if(upgradeItem.getUpgradeType() instanceof CapacityUpgrade)
					{
						limit += upgradeItem.getDefaultUpgradeData().getIntValue(CapacityUpgrade.CAPACITY);
					}
				}
			}
		}
		return limit;
	}
	public TraderItemStorage getStorage();
	public void markTradesDirty();
	public Container getUpgradeInventory();
	public void markUpgradesDirty();
	public void markStorageDirty();
	//public void openItemEditMenu(Player player, int tradeIndex);
	public ItemTraderSettings getItemSettings();
	public void markItemSettingsDirty();
	//Open menu functions
	//public ITradeRuleScreenHandler getRuleScreenHandler();
	public void sendTradeRuleUpdateMessage(int tradeIndex, ResourceLocation type, CompoundTag updateInfo);
	//public void sendSetTradeItemMessage(int tradeIndex, ItemStack sellItem, int slot);
	//public void sendSetTradePriceMessage(int tradeIndex, CoinValue newPrice, String newCustomName, ItemTradeType newTradeType);
	
	default List<? extends ITradeData> getTradeInfo() { return this.getAllTrades(); }
	
	public default TradeResult ExecuteTrade(TradeContext context, int tradeIndex)
	{

		ItemTradeData trade = this.getTrade(tradeIndex);
		//Abort if the trade is null
		if(trade == null)
		{
			LightmansCurrency.LogError("Trade at index " + tradeIndex + " is null. Cannot execute trade!");
			return TradeResult.FAIL_INVALID_TRADE;
		}
		
		//Abort if the trade is not valid
		if(!trade.isValid())
		{
			LightmansCurrency.LogWarning("Trade at index " + tradeIndex + " is not a valid trade. Cannot execute trade.");
			return TradeResult.FAIL_INVALID_TRADE;
		}
		
		if(!context.hasPlayerReference())
			return TradeResult.FAIL_NULL;
		
		//Check if the player is allowed to do the trade
		if(this.runPreTradeEvent(context.getPlayerReference(), trade).isCanceled())
			return TradeResult.FAIL_TRADE_RULE_DENIAL;
		
		//Get the cost of the trade
		CoinValue price = this.runTradeCostEvent(context.getPlayerReference(), trade).getCostResult();
		
		//Process a sale
		if(trade.isSale())
		{
			//Abort if not enough items in inventory
			if(!trade.hasStock(context) && !this.getCoreSettings().isCreative())
			{
				LightmansCurrency.LogDebug("Not enough items in storage to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_OUT_OF_STOCK;
			}
			
			//Abort if not enough room to put the sold item
			if(!context.canFitItems(trade.getSellItem(0), trade.getSellItem(1)))
			{
				LightmansCurrency.LogInfo("Not enough room for the output item. Aborting trade!");
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			}
			
			if(!context.getPayment(price))
			{
				LightmansCurrency.LogDebug("Not enough money is present for the trade at index " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_CANNOT_AFFORD;
			}
			
			//We have enough money, and the trade is valid. Execute the trade
			//Get the trade itemStack
			//Give the trade item
			if(!context.putItem(trade.getSellItem(0)))//If there's not enough room to give the item to the output item, abort the trade
			{
				LightmansCurrency.LogError("Not enough room for the output item. Giving refund & aborting Trade!");
				//Give a refund
				context.givePayment(price);
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			}
			if(!context.putItem(trade.getSellItem(1)))
			{
				LightmansCurrency.LogError("Not enough room for the output item. Giving refund & aborting Trade!");
				//Give a refund
				context.collectItem(trade.getSellItem(0));
				context.givePayment(price);
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			}
			
			//Log the successful trade
			this.getLogger().AddLog(context.getPlayerReference(), trade, price, this.getCoreSettings().isCreative());
			this.markLoggerDirty();
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.getCoreSettings().isCreative())
			{
				//Remove the sold items from storage
				trade.RemoveItemsFromStorage(this.getStorage());
				this.markStorageDirty();
				//Give the paid cost to storage
				this.addStoredMoney(price);
				
			}
			
			//Push the post-trade event
			this.runPostTradeEvent(context.getPlayerReference(), trade, price);
			
			return TradeResult.SUCCESS;
			
		}
		//Process a purchase
		else if(trade.isPurchase())
		{
			//Abort if not enough items in the item slots
			if(!context.hasItems(trade.getSellItem(0), trade.getSellItem(1)))
			{
				LightmansCurrency.LogDebug("Not enough items in the item slots to make the purchase.");
				return TradeResult.FAIL_CANNOT_AFFORD;
			}
			
			//Abort if not enough room to store the purchased items (unless we're creative)
			if(!trade.hasSpace(this) && !this.getCoreSettings().isCreative())
			{
				LightmansCurrency.LogDebug("Not enough room in storage to store the purchased items.");
				return TradeResult.FAIL_NO_INPUT_SPACE;
			}
			//Abort if not enough money to pay them back
			if(!trade.hasStock(context) && !this.getCoreSettings().isCreative())
			{
				LightmansCurrency.LogDebug("Not enough money in storage to pay for the purchased items.");
				return TradeResult.FAIL_OUT_OF_STOCK;
			}
			//Passed the checks. Take the item(s) from the input slot
			context.collectItem(trade.getSellItem(0));
			context.collectItem(trade.getSellItem(1));
			//Put the payment in the purchasers wallet, coin slot, etc.
			context.givePayment(price);
			
			//Log the successful trade
			this.getLogger().AddLog(context.getPlayerReference(), trade, price, this.getCoreSettings().isCreative());
			this.markLoggerDirty();
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.getCoreSettings().isCreative())
			{
				//Put the item in storage
				this.getStorage().forceAddItem(trade.getSellItem(0));
				this.getStorage().forceAddItem(trade.getSellItem(1));
				this.markStorageDirty();
				//Remove the coins from storage
				this.removeStoredMoney(price);
			}
			
			//Push the post-trade event
			this.runPostTradeEvent(context.getPlayerReference(), trade, price);
			
			return TradeResult.SUCCESS;
			
		}
		//Process a barter
		else if(trade.isBarter())
		{
			//Abort if not enough items in the item slots
			if(!context.hasItems(trade.getBarterItem(0), trade.getBarterItem(1)))
			{
				LightmansCurrency.LogDebug("Not enough items in the item slots to make the barter.");
				return TradeResult.FAIL_CANNOT_AFFORD;
			}
			//Abort if not enough room to store the purchased items (unless we're creative)
			if(!trade.hasSpace(this) && !this.getCoreSettings().isCreative())
			{
				LightmansCurrency.LogDebug("Not enough room in storage to store the purchased items.");
				return TradeResult.FAIL_NO_INPUT_SPACE;
			}
			//Abort if not enough items in inventory
			if(!trade.hasStock(context) && !this.getCoreSettings().isCreative())
			{
				LightmansCurrency.LogDebug("Not enough items in storage to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_OUT_OF_STOCK;
			}
			
			//Passed the checks. Take the item(s) from the input slot
			context.collectItem(trade.getBarterItem(0));
			context.collectItem(trade.getBarterItem(1));
			//Check if there's room for the new items
			if(!context.putItem(trade.getSellItem(0)))
			{
				//Abort if no room for the sold item
				LightmansCurrency.LogDebug("Not enough room for the output item. Aborting trade!");
				context.putItem(trade.getBarterItem(0));
				context.putItem(trade.getBarterItem(1));
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			}
			if(!context.putItem(trade.getSellItem(1)))
			{
				//Abort if no room for the sold item
				LightmansCurrency.LogDebug("Not enough room for the output item. Aborting trade!");
				context.collectItem(trade.getSellItem(0));
				context.putItem(trade.getBarterItem(0));
				context.putItem(trade.getBarterItem(1));
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			}
			
			//Log the successful trade
			this.getLogger().AddLog(context.getPlayerReference(), trade, CoinValue.EMPTY, this.getCoreSettings().isCreative());
			this.markLoggerDirty();
			
			//Ignore editing internal storage if this is flagged as creative.
			if(!this.getCoreSettings().isCreative())
			{
				//Put the item in storage
				this.getStorage().forceAddItem(trade.getBarterItem(0));
				this.getStorage().forceAddItem(trade.getBarterItem(1));
				//Remove the item from storage
				trade.RemoveItemsFromStorage(this.getStorage());
				this.markStorageDirty();
			}
			
			//Push the post-trade event
			this.runPostTradeEvent(context.getPlayerReference(), trade, price);
			
			return TradeResult.SUCCESS;
			
		}
		
		return TradeResult.FAIL_INVALID_TRADE;
	}
	
	public default void initStorageTabs(TraderStorageMenu menu) {
		//Storage tab
		menu.setTab(TraderStorageTab.TAB_TRADE_STORAGE, new ItemStorageTab(menu));
		//Item Trade interaction tab
		menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED, new ItemTradeEditTab(menu));
	}
	
}
