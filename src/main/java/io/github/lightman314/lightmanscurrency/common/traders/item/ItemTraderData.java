package io.github.lightman314.lightmanscurrency.common.traders.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.traders.*;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.blockentity.handler.TraderItemHandler;
import io.github.lightman314.lightmanscurrency.common.traders.item.settings.ItemTradeSettings;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.TradeOfferUpgrade;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.AddRemoveTradeNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.ItemTradeNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.OutOfStockNotification;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.TraderItemStorage.ITraderItemFilter;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.item.ItemStorageTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderstorage.item.ItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemTraderData extends InputTraderData implements ITraderItemFilter, IFlexibleOfferTrader, TraderItemHandler.IItemStorageProvider {

	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(Upgrades.ITEM_CAPACITY, Upgrades.TRADE_OFFERS);
	
	public static final int DEFAULT_STACK_LIMIT = 64 * 9;
	
	public static final TraderType<ItemTraderData> TYPE = new TraderType<>(VersionUtil.lcResource( "item_trader"), ItemTraderData::new);
	
	TraderItemHandler<ItemTraderData> itemHandler = new TraderItemHandler<>(this);
	
	public IItemHandler getItemHandler(Direction relativeSide) { return this.itemHandler.getHandler(relativeSide); }
	
	protected TraderItemStorage storage = new TraderItemStorage(this);
	public final TraderItemStorage getStorage() { return this.storage; }
	public void markStorageDirty() { this.markDirty(this::saveStorage); }

	private int baseTradeCount = 0;
	protected List<ItemTradeData> trades;
	
	@Override
	public boolean allowAdditionalUpgradeType(UpgradeType type) { return ALLOWED_UPGRADES.contains(type); }

	private ItemTraderData(){ this(TYPE); }
	protected ItemTraderData(TraderType<?> type) {
		super(type);
		this.trades = ItemTradeData.listOfSize(1, this.tradeBuilder(true));
		this.validateTradeRestrictions();
	}
	
	public ItemTraderData(int tradeCount, Level level, BlockPos pos) { this(TYPE, tradeCount, level, pos); }
	
	protected ItemTraderData(TraderType<?> type, int tradeCount, Level level, BlockPos pos)
	{
		super(type, level, pos);
		this.trades = ItemTradeData.listOfSize(tradeCount, this.tradeBuilder(true));
		this.baseTradeCount = tradeCount;
		this.validateTradeRestrictions();
	}

	@Override
	protected void registerNodes(Consumer<SettingsNode> builder) {
		super.registerNodes(builder);
		builder.accept(new ItemTradeSettings(this));
	}

	@Override
	protected boolean allowVoidUpgrade() { return true; }

    @Override
    public List<TradeDirection> validDirectionOptions() { return ImmutableList.of(TradeDirection.SALE,TradeDirection.PURCHASE,TradeDirection.BARTER); }

    @Override
	public void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		super.saveAdditional(compound,lookup);
		
		this.saveStorage(compound, lookup);
		this.saveTrades(compound, lookup);
		
	}
	
	protected final void saveStorage(CompoundTag compound, HolderLookup.Provider lookup) {
		this.storage.save(compound, "ItemStorage", lookup);
	}
	
	protected final void saveTrades(CompoundTag compound, HolderLookup.Provider lookup) {
		compound.putInt("BaseTradeCount", this.baseTradeCount);
		ItemTradeData.saveAllData(compound, this.trades, lookup);
	}
	
	@Override
	public void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		super.loadAdditional(compound,lookup);
		
		if(compound.contains("ItemStorage"))
			this.storage.load(compound, "ItemStorage", lookup);
		
		if(compound.contains(TradeData.DEFAULT_KEY))
		{
			this.trades = ItemTradeData.loadAllData(compound,this.tradeBuilder(!this.isPersistent()),lookup);
			this.validateTradeRestrictions();
		}
		if(compound.contains("BaseTradeCount"))
			this.baseTradeCount = compound.getInt("BaseTradeCount");
		if(this.baseTradeCount <= 0) //Reset base trade count to current trade count if the current value is invalid
			this.baseTradeCount = this.trades.size();

	}

	protected Supplier<ItemTradeData> tradeBuilder(boolean validateRules) { return () -> ItemTradeData.create(validateRules); }
	
	@Override
	public int getTradeCount() { return this.trades.size(); }

	@Override
	public void addTrade(Player requestor)
	{
		if(this.isClient())
			return;
		if(LCAdminMode.isAdminPlayer(requestor) && this.baseTradeCount < TraderData.GLOBAL_TRADE_LIMIT)
		{

			this.baseTradeCount++;
			this.refactorTrades();
			
			this.pushLocalNotification(new AddRemoveTradeNotification(PlayerReference.of(requestor), true, this.getTradeCount()));
			
		}
		else
			Permissions.PermissionWarning(requestor, "add a trade slot", Permissions.ADMIN_MODE);
	}
	
	public void removeTrade(Player requestor)
	{
		if(this.isClient())
			return;
		if(LCAdminMode.isAdminPlayer(requestor) && this.baseTradeCount > 1)
		{

			this.baseTradeCount--;
			this.refactorTrades();
			
			this.pushLocalNotification(new AddRemoveTradeNotification(PlayerReference.of(requestor), false, this.getTradeCount()));
			
		}
		else
			Permissions.PermissionWarning(requestor, "remove a trade slot", Permissions.ADMIN_MODE);
	}

	@Override
	public final void refactorTrades() {
		int newCount = MathUtil.clamp(this.baseTradeCount + TradeOfferUpgrade.getBonusTrades(this.getUpgrades()), 1, TraderData.GLOBAL_TRADE_LIMIT);
		if(newCount != this.trades.size())
			this.overrideTradeCount(newCount);
	}

	public void overrideTradeCount(int newTradeCount)
	{
		int tradeCount = MathUtil.clamp(newTradeCount,1,TraderData.GLOBAL_TRADE_LIMIT);
		if(this.trades.size() == tradeCount)
			return;
		List<ItemTradeData> oldTrades = this.trades;
		this.trades = ItemTradeData.listOfSize(tradeCount, this.tradeBuilder(!this.isPersistent()));
		//Write the old trade data into the array.
		for(int i = 0; i < oldTrades.size() && i < this.trades.size(); i++)
		{
			this.trades.set(i, oldTrades.get(i));
		}
		this.validateTradeRestrictions();
		//Send an update to the client
		if(this.isServer())
		{
			//Send update packet
			this.markTradesDirty();
		}
	}
	
	public final void validateTradeRestrictions() {
		for(int i = 0; i < this.trades.size(); ++i)
		{
			ItemTradeData trade = this.trades.get(i);
			trade.setRestriction(this.getTradeRestriction(i));
		}
	}
	
	protected ItemTradeRestriction getTradeRestriction(int tradeIndex) { return ItemTradeRestriction.NONE; }
	
	public ItemTradeData getTrade(int tradeSlot)
	{
		if(tradeSlot < 0 || tradeSlot >= this.trades.size())
		{
			LightmansCurrency.LogError("Cannot get trade in index " + tradeSlot + " from a trader with only " + this.trades.size() + " trades.");
			return this.tradeBuilder(false).get();
		}
		return this.trades.get(tradeSlot);
	}
	
	
    @Override
	public List<ItemTradeData> getTradeData() { return this.trades; }
	
	public int getTradeStock(int tradeSlot)
	{
		ItemTradeData trade = getTrade(tradeSlot);
		if(trade.sellItemsDefined())
		{
			if(this.isCreative())
				return Integer.MAX_VALUE;
			else
				return trade.stockCount(this);
		}
		return 0;
	}
	
	@Override
	public IconData inputSettingsTabIcon() { return ItemIcon.ofItem(Items.HOPPER); }

	@Override
	public MutableComponent inputSettingsTabTooltip() { return LCText.TOOLTIP_TRADER_SETTINGS_INPUT_ITEM.get(); }

	@Override
	public IconData getIcon() { return IconUtil.ICON_TRADER; }

	@Override
	protected void saveAdditionalToJson(JsonObject json, HolderLookup.Provider lookup) {
		
		JsonArray trades = new JsonArray();
		for(ItemTradeData trade : this.trades)
		{
			if(trade.isValid())
			{
				JsonObject tradeData = new JsonObject();
				JsonArray ignoreNBTData = new JsonArray();
				tradeData.addProperty("TradeType", trade.getTradeDirection().name());
				if(trade.getSellItem(0).isEmpty())
				{
					tradeData.add("SellItem", FileUtil.convertItemStack(trade.getSellItem(1),lookup));
					if(trade.hasCustomName(1))
						tradeData.addProperty("DisplayName", trade.getCustomName(1));
					//Manually assign to the 0th index, as this is what the loaded trade will acknowledge
					if(!trade.getEnforceNBT(1))
						ignoreNBTData.add(0);
				}
				else
				{
					tradeData.add("SellItem", FileUtil.convertItemStack(trade.getSellItem(0),lookup));
					if(trade.hasCustomName(0))
						tradeData.addProperty("DisplayName", trade.getCustomName(0));
					if(!trade.getEnforceNBT(0))
						ignoreNBTData.add(0);
					if(!trade.getSellItem(1).isEmpty())
					{
						tradeData.add("SellItem2", FileUtil.convertItemStack(trade.getSellItem(1),lookup));
						if(trade.hasCustomName(1))
							tradeData.addProperty("DisplayName2", trade.getCustomName(1));
						if(!trade.getEnforceNBT(1))
							ignoreNBTData.add(1);
					}
				}
				
				if(trade.isSale() || trade.isPurchase())
					tradeData.add("Price", trade.getCost().toJson());
				
				if(trade.isBarter())
				{
					if(trade.getBarterItem(0).isEmpty())
					{
						tradeData.add("BarterItem", FileUtil.convertItemStack(trade.getBarterItem(1),lookup));
						//Manually assign to the 2nd index, as this is what the loaded trade will acknowledge
						if(!trade.getEnforceNBT(3))
							ignoreNBTData.add(2);
					}
					else
					{
						tradeData.add("BarterItem", FileUtil.convertItemStack(trade.getBarterItem(0),lookup));
						if(!trade.getEnforceNBT(2))
							ignoreNBTData.add(2);
						if(!trade.getBarterItem(1).isEmpty())
						{
							tradeData.add("BarterItem2", FileUtil.convertItemStack(trade.getBarterItem(1),lookup));
							if(!trade.getEnforceNBT(3))
								ignoreNBTData.add(3);
						}
					}
				}

				//Save ignored NBT slots (if relevant)
				if(!ignoreNBTData.isEmpty())
					tradeData.add("IgnoreNBT", ignoreNBTData);
				
				JsonArray ruleData = TradeRule.saveRulesToJson(trade.getRules(),lookup);
				if(!ruleData.isEmpty())
					tradeData.add("Rules", ruleData);
				
				trades.add(tradeData);
			}
		}

		//Save relevant storage (for sales that have randomized items to output)
		JsonArray storageData = new JsonArray();
		for(ItemStack item : this.storage.getContents())
		{
			boolean shouldWrite = false;
			for(int i = 0; i < this.trades.size() && !shouldWrite; ++i)
			{
				ItemTradeData trade = this.trades.get(i);
				if(trade.isValid() && trade.shouldStorageItemBeSaved(item))
					shouldWrite = true;
			}
			if(shouldWrite)
				storageData.add(FileUtil.convertItemStack(item,lookup));
		}
		if(!storageData.isEmpty())
			json.add("RelevantStorage", storageData);
		
		json.add("Trades", trades);
		
	}

	@Override
	protected void loadAdditionalFromJson(JsonObject json, HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {

		JsonArray trades = GsonHelper.getAsJsonArray(json, "Trades");
		this.trades = new ArrayList<>();
		for(int i = 0; i < trades.size() && this.trades.size() < TraderData.GLOBAL_TRADE_LIMIT; ++i)
		{
			try {
				JsonObject tradeData = trades.get(i).getAsJsonObject();
				
				ItemTradeData newTrade = ItemTradeData.create(false);
				//Sell Item
				newTrade.setItem(FileUtil.parseItemStack(GsonHelper.getAsJsonObject(tradeData, "SellItem"),lookup), 0);
				if(tradeData.has("SellItem2"))
					newTrade.setItem(FileUtil.parseItemStack(GsonHelper.getAsJsonObject(tradeData, "SellItem2"),lookup), 1);
				//Trade Type
				if(tradeData.has("TradeType"))
					newTrade.setTradeType(ItemTradeData.loadTradeType(GsonHelper.getAsString(tradeData, "TradeType")));
				//Trade Price
				if(tradeData.has("Price"))
				{
					if(newTrade.isBarter())
						LightmansCurrency.LogWarning("Price is being defined for a barter trade. Price will be ignored.");
					else
						newTrade.setCost(MoneyValue.loadFromJson(tradeData.get("Price")));
				}
				else if(!newTrade.isBarter())
				{
					LightmansCurrency.LogWarning("Price is not defined on a non-barter trade. Price will be assumed to be free.");
					newTrade.setCost(MoneyValue.free());
				}
				if(tradeData.has("BarterItem"))
				{
					if(newTrade.isBarter())
					{
						newTrade.setItem(FileUtil.parseItemStack(GsonHelper.getAsJsonObject(tradeData,"BarterItem"),lookup), 2);
						if(tradeData.has("BarterItem2"))
							newTrade.setItem(FileUtil.parseItemStack(GsonHelper.getAsJsonObject(tradeData,"BarterItem2"),lookup), 3);
					}
					else
					{
						LightmansCurrency.LogWarning("BarterItem is being defined for a non-barter trade. Barter item will be ignored.");
					}
				}
				if(tradeData.has("DisplayName"))
					newTrade.setCustomName(0, GsonHelper.getAsString(tradeData, "DisplayName"));
				if(tradeData.has("DisplayName2"))
					newTrade.setCustomName(1, GsonHelper.getAsString(tradeData, "DisplayName2"));
				if(tradeData.has("Rules"))
					newTrade.setRules(TradeRule.Parse(GsonHelper.getAsJsonArray(tradeData, "Rules"), newTrade, lookup));
				if(tradeData.has("IgnoreNBT"))
				{
					JsonArray ignoreNBTData = GsonHelper.getAsJsonArray(tradeData,"IgnoreNBT");
					for(int j = 0; j < ignoreNBTData.size(); ++j)
					{
						int slot = ignoreNBTData.get(j).getAsInt();
						newTrade.setEnforceNBT(slot, false);
					}
				}
				
				this.trades.add(newTrade);
				
			} catch(Exception e) { LightmansCurrency.LogError("Error parsing item trade at index " + i, e); }
		}
		
		if(this.trades.isEmpty())
			throw new JsonSyntaxException("Trader has no valid trades!");

		List<ItemStack> storage = new ArrayList<>();
		if(json.has("RelevantStorage"))
		{
			JsonArray storageData = json.getAsJsonArray("RelevantStorage");
			for(int i = 0; i < storageData.size(); ++i)
			{
				try{
					ItemStack item = FileUtil.parseItemStack(storageData.get(i).getAsJsonObject(),lookup);
					storage.add(item);
				} catch(JsonSyntaxException | ResourceLocationException e) { LightmansCurrency.LogError("Error parsing storage item at index " + i, e); }
			}
		}

		this.storage = new TraderItemStorage.LockedTraderStorage(this, storage);
		
	}

	@Override
	protected void saveAdditionalPersistentData(CompoundTag compound, HolderLookup.Provider lookup) {
		ListTag tradePersistentData = new ListTag();
		boolean tradesAreRelevant = false;
		for (ItemTradeData trade : this.trades) {
			CompoundTag ptTag = new CompoundTag();
			if (TradeRule.savePersistentData(ptTag, trade.getRules(), "RuleData", lookup))
				tradesAreRelevant = true;
			tradePersistentData.add(ptTag);
		}
		if(tradesAreRelevant)
			compound.put("PersistentTradeData", tradePersistentData);
	}

	@Override
	protected void loadAdditionalPersistentData(CompoundTag compound, HolderLookup.Provider lookup) {
		if(compound.contains("PersistentTradeData"))
		{
			ListTag tradePersistentData = compound.getList("PersistentTradeData", Tag.TAG_COMPOUND);
			for(int i = 0; i < tradePersistentData.size() && i < this.trades.size(); ++i)
			{
				ItemTradeData trade = this.trades.get(i);
				CompoundTag ptTag = tradePersistentData.getCompound(i);
				TradeRule.loadPersistentData(ptTag, trade.getRules(), "RuleData", lookup);
			}
		}
	}

	@Override
	protected void getAdditionalContents(List<ItemStack> results) {
		
		//Add item storage contents
		results.addAll(this.storage.getSplitContents());
		
	}

	@Override
	protected TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {
		ItemTradeData trade = this.getTrade(tradeIndex);
		//Abort if the trade is null
		if(trade == null)
		{
			//LightmansCurrency.LogDebug("Trade at index " + tradeIndex + " is null. Cannot execute trade!");
			return TradeResult.FAIL_INVALID_TRADE;
		}
		
		//Abort if the trade is not valid
		if(!trade.isValid())
		{
			//LightmansCurrency.LogDebug("Trade at index " + tradeIndex + " is not a valid trade. Cannot execute trade.");
			return TradeResult.FAIL_INVALID_TRADE;
		}
		
		if(!context.hasPlayerReference())
			return TradeResult.FAIL_NULL;
		
		//Check if the player is allowed to do the trade
		if(this.runPreTradeEvent(trade, context).isCanceled())
			return TradeResult.FAIL_TRADE_RULE_DENIAL;
		
		//Get the cost of the trade
		//Update: Use TradeData#getCost(TradeContext) as it will run the trade cost event automatically
		MoneyValue price = trade.getCost(context);
		
		//Process a sale
		if(trade.isSale())
		{
			if(trade.outOfStock(context) && !this.isCreative())
			{
				//LightmansCurrency.LogDebug("Not enough items in storage to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_OUT_OF_STOCK;
			}

			//Randomize the items to be sold
			List<ItemStack> soldItems = trade.getRandomSellItems(this);
			//Abort if not enough items in inventory
			if(soldItems == null)
			{
                LightmansCurrency.LogWarning("Trade failed to collect items being sold from storage, in spite of the trade claiming to have items in stock!");
				return TradeResult.FAIL_OUT_OF_STOCK;
			}

			//Abort if not enough room to put the sold item
			if(!context.canFitItems(soldItems))
			{
				//LightmansCurrency.LogDebug("Not enough room for the output item. Aborting trade!");
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			}
			
			if(!context.getPayment(price))
			{
				//LightmansCurrency.LogDebug("Not enough money is present for the trade at index " + tradeIndex + ". Cannot execute trade." +
				//		"\nPrice: " + price.getString("Null") + "\nAvailable Funds: " + context.getAvailableFunds().getString());
				return TradeResult.FAIL_CANNOT_AFFORD;
			}
			
			//We have enough money, and the trade is valid. Execute the trade
			//Get the trade itemStack
			//Give the trade item
			for(int i = 0; i < soldItems.size(); ++i)
			{
				if(!context.putItem(soldItems.get(i)))//If there's not enough room to give the item to the output item, abort the trade
				{
					LightmansCurrency.LogError("Not enough room for the output item. Giving refund & aborting Trade!");
					//Collect the items already given
					for(int x = 0; x < i; ++x)
						context.collectItem(soldItems.get(x));
					//Give a refund
					context.givePayment(price);
					return TradeResult.FAIL_NO_OUTPUT_SPACE;
				}
			}

			//Give the paid cost to storage
			MoneyValue taxesPaid = MoneyValue.empty();
			if(this.canStoreMoney())
				taxesPaid = this.addStoredMoney(price, true);

			//Ignore editing internal storage if this is flagged as creative.
			if(!this.isCreative())
			{
				//Remove the sold items from storage
				trade.RemoveItemsFromStorage(this.getStorage(),soldItems);
				this.markStorageDirty();

			}

			//Handle Stats
			this.incrementStat(StatKeys.Traders.MONEY_EARNED, price);
			if(!taxesPaid.isEmpty())
				this.incrementStat(StatKeys.Taxables.TAXES_PAID,taxesPaid);

			//Push Notification
			this.pushNotification(ItemTradeNotification.create(trade, price, context.getPlayerReference(), this.getNotificationCategory(), taxesPaid));
			//Push out of stock notification
			if(!trade.hasStock(this))
				this.pushNotification(OutOfStockNotification.create(this.getNotificationCategory(), tradeIndex));

			//Push the post-trade event
			this.runPostTradeEvent(trade,context,price,taxesPaid,soldItems);
			
			return TradeResult.SUCCESS;
			
		}
		//Process a purchase
		else if(trade.isPurchase())
		{
			List<ItemStack> collectableItems = context.getCollectableItems(trade.getItemRequirement(0), trade.getItemRequirement(1));
			//Abort if not enough items in the item slots
			if(!context.hasItems(collectableItems))
			{
				//LightmansCurrency.LogDebug("Not enough items in the item slots to make the purchase.");
				return TradeResult.FAIL_CANNOT_AFFORD;
			}
			
			//Abort if not enough room to store the purchased items (unless we're creative)
			if(!trade.hasSpace(this, collectableItems) && !this.isCreative())
			{
				//LightmansCurrency.LogDebug("Not enough room in storage to store the purchased items.");
				return TradeResult.FAIL_NO_INPUT_SPACE;
			}
			//Abort if not enough money to pay them back
			if(trade.outOfStock(context) && !this.isCreative())
			{
				//LightmansCurrency.LogDebug("Not enough money in storage to pay for the purchased items.");
				return TradeResult.FAIL_OUT_OF_STOCK;
			}
			//Passed the checks. Take the item(s) from the input slot
			context.collectItems(collectableItems);
			//Put the payment in the purchasers' wallet, coin slot, etc.
			context.givePayment(price);

			MoneyValue taxesPaid = MoneyValue.empty();

			//Ignore editing internal storage if this is flagged as creative or a void upgrade is equipped
			if(this.shouldStoreGoods())
			{
				//Put the item(s) in storage
				for(ItemStack item : collectableItems)
					this.getStorage().forceAddItem(item);
				this.markStorageDirty();
			}
			if(!this.isCreative())
			{
				//Remove the coins from storage
				taxesPaid = this.removeStoredMoney(price, true);
			}

			//Handle Stats
			this.incrementStat(StatKeys.Traders.MONEY_PAID, price);
			if(!taxesPaid.isEmpty())
				this.incrementStat(StatKeys.Taxables.TAXES_PAID,taxesPaid);

			//Push Notification
			this.pushNotification(ItemTradeNotification.create(trade, price, context.getPlayerReference(), this.getNotificationCategory(), taxesPaid));
			//Push out of stock notification
			if(!trade.hasStock(this))
				this.pushNotification(OutOfStockNotification.create(this.getNotificationCategory(), tradeIndex));

			//Push the post-trade event
			this.runPostTradeEvent(trade,context,price,taxesPaid,collectableItems);
			
			return TradeResult.SUCCESS;
			
		}
		//Process a barter
		else if(trade.isBarter())
		{
			//Collect items that will be taken from the customer.
			List<ItemStack> collectableItems = context.getCollectableItems(trade.getItemRequirement(2), trade.getItemRequirement(3));
			//Abort if not enough items in the item slots
			if(collectableItems == null)
			{
				//LightmansCurrency.LogDebug("Collectable items returned a null list!");
				return TradeResult.FAIL_CANNOT_AFFORD;
			}

			//Abort if not enough room to store the purchased items (unless we're creative)
			if(!trade.hasSpace(this, collectableItems) && !this.isCreative())
			{
				//LightmansCurrency.LogDebug("Not enough room in storage to store the purchased items.");
				return TradeResult.FAIL_NO_INPUT_SPACE;
			}

			if(trade.outOfStock(context) && !this.isCreative())
			{
				//LightmansCurrency.LogDebug("Not enough items in storage to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_OUT_OF_STOCK;
			}

			List<ItemStack> soldItems = trade.getRandomSellItems(this);
			//Abort if not enough items in inventory
			if(soldItems == null)
			{
				//LightmansCurrency.LogDebug("Not enough items in storage to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_OUT_OF_STOCK;
			}

			//Abort if no space to put the sold items
			if(!context.canFitItems(soldItems))
			{
				//LightmansCurrency.LogDebug("Not enough space to store the purchased items.");
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			}
			
			//Passed the checks. Take the item(s) from the input slot
			context.collectItems(collectableItems);
			//Check if there's room for the new items
			for(int i = 0; i < soldItems.size(); ++i)
			{
				//If there's not enough room to give the item to the output item, abort the trade
				if(!context.putItem(soldItems.get(i)))
				{
					LightmansCurrency.LogError("Not enough room for the output item. Giving refund & aborting Trade!");
					//Collect the items already given
					for(int x = 0; x < i; ++x)
						context.collectItem(soldItems.get(x));
					//Give a refund
					context.givePayment(price);
					return TradeResult.FAIL_NO_OUTPUT_SPACE;
				}
			}

			//Ignore editing internal storage if this is flagged as creative or a void upgrade is equipped
			boolean storageChanged = false;
			if(this.shouldStoreGoods())
			{
				//Put the item in storage
				for(ItemStack item : collectableItems)
					this.storage.forceAddItem(item);
				storageChanged = true;
			}
			if(!this.isCreative())
			{
				//Remove the item from storage
				trade.RemoveItemsFromStorage(this.getStorage(), soldItems);
				storageChanged = true;
			}
			if(storageChanged)
				this.markStorageDirty();

			//Push Notification
			this.pushNotification(ItemTradeNotification.create(trade, price, context.getPlayerReference(), this.getNotificationCategory(), MoneyValue.empty()));
			//Push out of stock notification
			if(!trade.hasStock(this))
				this.pushNotification(OutOfStockNotification.create(this.getNotificationCategory(), tradeIndex));

            List<List<ItemStack>> product = Lists.newArrayList(soldItems,collectableItems);
			//Push the post-trade event
			this.runPostTradeEvent(trade,context,price,MoneyValue.empty(),product);

			return TradeResult.SUCCESS;
		}
		
		return TradeResult.FAIL_INVALID_TRADE;
	}

	@Override
	public boolean canMakePersistent() { return true; }

	@Override
	public void initStorageTabs(ITraderStorageMenu menu) {
		//Storage tab
		menu.setTab(TraderStorageTab.TAB_TRADE_STORAGE, new ItemStorageTab(menu));
		//Item Trade interaction tab
		menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED, new ItemTradeEditTab(menu));
	}

	@Override
	public boolean isItemRelevant(ItemStack item) {
		for(ItemTradeData trade : this.trades)
		{
			if(trade.allowItemInStorage(item))
				return true;
		}
		return false;
	}

	@Override
	public boolean allowExtraction(ItemStack stack) {
		for(ItemTradeData trade : this.trades)
		{
			if(trade.isValid() && (trade.isSale() || trade.isBarter()))
			{
				for(int i = 0; i < 2; ++i)
				{
					if(trade.getItemRequirement(i).test(stack))
						return false;
				}
			}
		}
		return true;
	}

	@Override
	public int getStorageStackLimit() {
		int limit = DEFAULT_STACK_LIMIT;
		for(int i = 0; i < this.getUpgrades().getContainerSize(); ++i)
		{
			ItemStack stack = this.getUpgrades().getItem(i);
			if(stack.getItem() instanceof UpgradeItem upgradeItem)
			{
				if(this.allowUpgrade(upgradeItem) && upgradeItem.getUpgradeType() == Upgrades.ITEM_CAPACITY)
					limit += UpgradeItem.getUpgradeData(stack).getIntValue(CapacityUpgrade.CAPACITY);
			}
		}
		return limit;
	}

	@Override
	protected void appendTerminalInfo(List<Component> list, @Nullable Player player) {
		int tradeCount = 0;
		int outOfStock = 0;
		for(ItemTradeData trade : this.trades)
		{
			if(trade.isValid())
			{
				tradeCount++;
				if(!this.isCreative() && !trade.hasStock(this))
					outOfStock++;
			}
		}
		list.add(LCText.TOOLTIP_NETWORK_TERMINAL_TRADE_COUNT.get(tradeCount));
		if(outOfStock > 0)
			list.add(LCText.TOOLTIP_NETWORK_TERMINAL_OUT_OF_STOCK_COUNT.get(outOfStock));
	}
}
