package io.github.lightman314.lightmanscurrency.common.universal_traders.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ItemShopLogger;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.RemoteTradeData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.RemoteTradeData.RemoteTradeResult;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainer;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageAddOrRemoveTrade2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenStorage2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageSetItemPrice2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageSetTradeItem2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageUpdateTradeRule2;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.handler.TraderItemHandler;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.ItemTraderSettings;
import io.github.lightman314.lightmanscurrency.trader.settings.ItemTraderSettings.ItemHandlerSettings;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData.ItemTradeType;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;

public class UniversalItemTraderData extends UniversalTraderData implements IItemTrader {
	
	public static final int TRADE_LIMIT = ItemTraderTileEntity.TRADE_LIMIT;
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_trader");
	
	public static final int VERSION = 1;
	
	TraderItemHandler itemHandler = new TraderItemHandler(this);
	
	public IItemHandler getItemHandler(Direction relativeSide)
	{
		ItemHandlerSettings setting = this.itemSettings.getHandlerSetting(relativeSide);
		return this.itemHandler.getHandler(setting);
	}
	
	private ItemTraderSettings itemSettings = new ItemTraderSettings(this, this::markItemSettingsDirty, this::sendSettingsUpdateToServer);
	
	int tradeCount = 1;
	List<ItemTradeData> trades = null;
	
	Inventory storage;
	
	private final ItemShopLogger logger = new ItemShopLogger();
	
	List<TradeRule> tradeRules = new ArrayList<>();
	
	public UniversalItemTraderData() { }
	
	public UniversalItemTraderData(PlayerReference owner, BlockPos pos, RegistryKey<World> world, UUID traderID, int tradeCount)
	{
		super(owner, pos, world, traderID);
		this.tradeCount = MathUtil.clamp(tradeCount, 1, TRADE_LIMIT);
		this.trades = ItemTradeData.listOfSize(this.tradeCount);
		this.storage = new Inventory(this.inventorySize());
		this.storage.addListener(this::markStorageDirty);
	}

	private void markStorageDirty(IInventory inventory) { this.markStorageDirty(); }
	
	@Override
	public void read(CompoundNBT compound)
	{
		if(compound.contains("TradeLimit", Constants.NBT.TAG_INT))
			this.tradeCount = MathUtil.clamp(compound.getInt("TradeLimit"), 1, ItemTraderTileEntity.TRADE_LIMIT);
		
		if(compound.contains(ItemTradeData.DEFAULT_KEY, Constants.NBT.TAG_LIST))
			this.trades = ItemTradeData.loadAllData(compound, this.tradeCount);
		
		if(this.storage == null)
		{
			this.storage = new Inventory(this.inventorySize());
			this.storage.addListener(this::markStorageDirty);
		}
		if(compound.contains("Storage", Constants.NBT.TAG_LIST))
		{
			this.storage = InventoryUtil.loadAllItems("Storage", compound, this.inventorySize());
			this.storage.addListener(this::markStorageDirty);
		}	
		
		this.logger.read(compound);
		
		if(compound.contains(TradeRule.DEFAULT_TAG, Constants.NBT.TAG_LIST))
			this.tradeRules = TradeRule.readRules(compound);
		
		if(compound.contains("ItemSettings", Constants.NBT.TAG_COMPOUND))
			this.itemSettings.load(compound.getCompound("ItemSettings"));
		
		super.read(compound);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{

		this.writeTrades(compound);
		this.writeStorage(compound);
		this.writeLogger(compound);
		this.writeRules(compound);
		this.writeItemSettings(compound);
		
		return super.write(compound);
		
	}
	
	protected final CompoundNBT writeTrades(CompoundNBT compound)
	{
		compound.putInt("TradeLimit", this.trades.size());
		ItemTradeData.saveAllData(compound, trades);
		return compound;
	}
	
	protected final CompoundNBT writeStorage(CompoundNBT compound)
	{
		InventoryUtil.saveAllItems("Storage", compound, this.storage);
		return compound;
	}
	
	protected final CompoundNBT writeLogger(CompoundNBT compound)
	{
		this.logger.write(compound);
		return compound;
	}
	
	protected final CompoundNBT writeRules(CompoundNBT compound)
	{
		TradeRule.writeRules(compound, this.tradeRules);
		return compound;
	}
	
	protected final CompoundNBT writeItemSettings(CompoundNBT compound)
	{
		compound.put("ItemSettings", this.itemSettings.save(new CompoundNBT()));
		return compound;
	}
	
	public int getTradeCount()
	{
		return this.tradeCount;
	}
	
	public int getTradeCountLimit()
	{
		return TRADE_LIMIT;
	}
	
	public void requestAddOrRemoveTrade(boolean isAdd)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade2(this.traderID, isAdd));
	}
	
	public void addTrade(PlayerEntity requestor)
	{
		if(!TradingOffice.isAdminPlayer(requestor))
		{
			Settings.PermissionWarning(requestor, "add trader slot", Permissions.ADMIN_MODE);
			return;
		}
		if(this.getTradeCount() >= TRADE_LIMIT)
			return;
		this.overrideTradeCount(this.tradeCount + 1);
		this.forceReopen();
		this.coreSettings.getLogger().LogAddRemoveTrade(requestor, true, this.tradeCount);
		this.markCoreSettingsDirty();
		
	}
	
	public void removeTrade(PlayerEntity requestor)
	{
		if(!TradingOffice.isAdminPlayer(requestor))
		{
			Settings.PermissionWarning(requestor, "remove trader slot", Permissions.ADMIN_MODE);
			return;
		}
		if(this.getTradeCount() <= 1)
			return;
		this.overrideTradeCount(this.tradeCount - 1);
		this.forceReopen();
		
		this.coreSettings.getLogger().LogAddRemoveTrade(requestor, false, this.tradeCount);
		this.markCoreSettingsDirty();
	}
	
	public void overrideTradeCount(int newTradeCount)
	{
		if(this.tradeCount == newTradeCount)
			return;
		this.tradeCount = MathUtil.clamp(newTradeCount, 1, TRADE_LIMIT);
		List<ItemTradeData> oldTrades = this.trades;
		this.trades = ItemTradeData.listOfSize(getTradeCount());
		//Write the old trade data into the array.
		for(int i = 0; i < oldTrades.size() && i < this.trades.size(); i++)
		{
			this.trades.set(i, oldTrades.get(i));
		}
		//Set the new inventory list
		IInventory oldInventory = this.storage;
		this.storage = new Inventory(this.inventorySize());
		for(int i = 0; i < this.storage.getSizeInventory() && i < oldInventory.getSizeInventory(); i++)
		{
			this.storage.setInventorySlotContents(i, oldInventory.getStackInSlot(i));
		}
		this.storage.addListener(this::markStorageDirty);
		//Attempt to place lost items into the available slots
		if(oldInventory.getSizeInventory() > this.inventorySize())
		{
			for(int i = this.inventorySize(); i < oldInventory.getSizeInventory(); i++)
			{
				InventoryUtil.TryPutItemStack(this.storage, oldInventory.getStackInSlot(i));
			}
		}
		//Mark as dirty (both trades & storage)
		CompoundNBT compound = this.writeTrades(new CompoundNBT());
		this.writeStorage(compound);
		this.markDirty(compound);
	}
	
	public ItemTradeData getTrade(int tradeIndex)
	{
		if(tradeIndex >= 0 && tradeIndex < getTradeCount())
		{
			return this.trades.get(tradeIndex);
		}
		return new ItemTradeData();
	}
	
	public int getTradeStock(int tradeIndex)
	{
		return getTrade(tradeIndex).stockCount(this);
	}
	
	public List<ItemTradeData> getAllTrades()
	{
		return this.trades;
	}
	
	public void markTradesDirty()
	{
		//Send update to the client with only the trade data.
		this.markDirty(this::writeTrades);
	}
	
	public ItemTraderSettings getItemSettings()
	{
		return this.itemSettings;
	}
	
	public List<Settings> getAdditionalSettings() { return Lists.newArrayList(); }
	
	public void markItemSettingsDirty()
	{
		this.markDirty(this::writeItemSettings);
	}
	
	public ItemShopLogger getLogger() { return this.logger; }
	
	public void clearLogger()
	{
		this.logger.clear();
		this.markLoggerDirty();
	}
	
	public void markLoggerDirty()
	{
		this.markDirty(this::writeLogger);
	}
	
	public int inventorySize()
	{
		return this.tradeCount * 9;
	}
	
	public IInventory getStorage()
	{
		return this.storage;
	}
	
	public void markStorageDirty()
	{
		this.markDirty(this::writeStorage);
	}
	
	/**
	 * Marks the storage and stored coins dirty
	 */
	public void markDirtyAfterTrade()
	{
		CompoundNBT compound = this.writeStorage(new CompoundNBT());
		this.writeStoredMoney(compound);
		this.markDirty(compound);
	}
	
	@Override
	public ResourceLocation getTraderType() {
		return TYPE;
	}
	
	@Override
	protected ITextComponent getDefaultName()
	{
		return new TranslationTextComponent("gui.lightmanscurrency.universaltrader.item");
	}

	@Override
	protected INamedContainerProvider getTradeMenuProvider() {
		return new TraderProvider(this.traderID);
	}

	@Override
	protected INamedContainerProvider getStorageMenuProvider() {
		return new StorageProvider(this.traderID);
	}
	
	protected INamedContainerProvider getItemEditMenuProvider(int tradeIndex) { return new ItemEditProvider(this.traderID, tradeIndex); }
	
	public void openItemEditMenu(PlayerEntity player, int tradeIndex)
	{
		INamedContainerProvider provider = getItemEditMenuProvider(tradeIndex);
		if(provider == null)
		{
			LightmansCurrency.LogError("No storage container provider was given for the universal trader of type " + this.getTraderType().toString());
			return;
		}
		if(player instanceof ServerPlayerEntity)
			NetworkHooks.openGui((ServerPlayerEntity)player, provider, new TradeIndexDataWriter(this.getTraderID(), tradeIndex));
		else
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
	}
	
	private static class TraderProvider implements INamedContainerProvider
	{
		final UUID traderID;
		private TraderProvider(UUID traderID) { this.traderID = traderID; }
		@Override
		public Container createMenu(int menuID, PlayerInventory inventory, PlayerEntity player) {
			return new ItemTraderContainer.ItemTraderContainerUniversal(menuID, inventory, this.traderID);
		}
		@Override
		public ITextComponent getDisplayName() { return new StringTextComponent(""); }
	}
	
	private static class StorageProvider implements INamedContainerProvider
	{
		final UUID traderID;
		private StorageProvider(UUID traderID) { this.traderID = traderID; }
		@Override
		public Container createMenu(int menuID, PlayerInventory inventory, PlayerEntity player) {
			return new ItemTraderStorageContainer.ItemTraderStorageContainerUniversal(menuID, inventory, this.traderID);
		}
		@Override
		public ITextComponent getDisplayName() { return new StringTextComponent(""); }
	}
	
	private static class ItemEditProvider implements INamedContainerProvider
	{
		final UUID traderID;
		final int tradeIndex;
		private ItemEditProvider(UUID traderID, int tradeIndex) { this.traderID = traderID; this.tradeIndex = tradeIndex; }
		
		@Override
		public Container createMenu(int menuID, PlayerInventory inventory, PlayerEntity player) {
			return new ItemEditContainer.UniversalItemEditContainer(menuID, inventory, this.traderID, this.tradeIndex);
		}

		@Override
		public ITextComponent getDisplayName() {
			return new StringTextComponent("");
		}
	}
	
	@Override
	public IconData getIcon() { return IconAndButtonUtil.ICON_TRADER; }
	
	@Override
	public int GetCurrentVersion() { return VERSION; }
	
	@Override
	protected void onVersionUpdate(int oldVersion) {
		//Updated so that the items in the trade data are not actual items, so place them in storage (if possible), or spawn them at the traders current position.
		if(oldVersion < 1)
		{
			for(ItemTradeData trade : trades)
			{
				ItemStack tradeStack = trade.getSellItem();
				if(!tradeStack.isEmpty())
					tradeStack = InventoryUtil.TryPutItemStack(this.storage, tradeStack);
				if(!tradeStack.isEmpty())
					LightmansCurrency.LogWarning(tradeStack.getCount() + " items lost during Universal Item Trader version update for trader " + this.traderID + ".");
			}
		}
	}
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		this.tradeRules.forEach(rule -> rule.beforeTrade(event));
	}
	
	@Override
	public void tradeCost(TradeCostEvent event) {
		this.tradeRules.forEach(rule -> rule.tradeCost(event));
	}

	@Override
	public void afterTrade(PostTradeEvent event) {
		this.tradeRules.forEach(rule -> rule.afterTrade(event));
	}

	public List<TradeRule> getRules() { return this.tradeRules; }
	
	public void setRules(List<TradeRule> rules) { this.tradeRules = rules; }
	
	public void addRule(TradeRule newRule)
	{
		if(newRule == null)
			return;
		//Confirm a lack of duplicate rules
		for(int i = 0; i < this.tradeRules.size(); i++)
		{
			if(newRule.type == this.tradeRules.get(i).type)
				return;
		}
		this.tradeRules.add(newRule);
	}
	
	public void removeRule(TradeRule rule)
	{
		if(this.tradeRules.contains(rule))
			this.tradeRules.remove(rule);
	}
	
	public void clearRules()
	{
		this.tradeRules.clear();
	}
	
	public void markRulesDirty()
	{
		this.markDirty(this::writeRules);
	}
	
	public ITradeRuleScreenHandler getRuleScreenHandler() { return new TradeRuleScreenHandler(this); }
	
	private static class TradeRuleScreenHandler implements ITradeRuleScreenHandler
	{
		
		private final UniversalItemTraderData trader;
		
		public TradeRuleScreenHandler(UniversalItemTraderData trader)
		{
			this.trader = trader;
		}
		
		@Override
		public ITradeRuleHandler ruleHandler() { return this.trader; }
		
		@Override
		public void reopenLastScreen()
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage2(this.trader.traderID));
		}
		
		public void updateServer(ResourceLocation type, CompoundNBT updateInfo)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdateTradeRule2(this.trader.traderID, type, updateInfo));
		}
		
		@Override
		public boolean stillValid() { return ClientTradingOffice.getData(this.trader.traderID) != null; }
		
	}
	
	@Override
	public RemoteTradeResult handleRemotePurchase(int tradeIndex, RemoteTradeData data) {

		if(tradeIndex < 0 || tradeIndex >= this.trades.size())
			return RemoteTradeResult.FAIL_INVALID_TRADE;

		ItemTradeData trade = this.getTrade(tradeIndex);
		//Abort if the trade is null
		if(trade == null)
			return RemoteTradeResult.FAIL_INVALID_TRADE;
		//Abort if the trade is not valid
		if(!trade.isValid())
			return RemoteTradeResult.FAIL_INVALID_TRADE;
		//Check if the player is allowed to do the trade
		if(this.runPreTradeEvent(data.getPlayerSource(), tradeIndex).isCanceled())
			return RemoteTradeResult.FAIL_TRADE_RULE_DENIAL;

		CoinValue price = this.runTradeCostEvent(data.getPlayerSource(), tradeIndex).getCostResult();

		//Execute a sale
		if(trade.isSale())
		{
			//Abort if not enough items in inventory
			if(!trade.hasStock(this) && !this.getCoreSettings().isCreative())
				return RemoteTradeResult.FAIL_OUT_OF_STOCK;
			//Abort if not enough room to put the sold item
			if(!data.canFitItem(trade.getSellItem()))
				return RemoteTradeResult.FAIL_NO_OUTPUT_SPACE;
			//Abort if not enough 
			if(!data.getPayment(price))
				return RemoteTradeResult.FAIL_CANNOT_AFFORD;

			//We have enough money, and the trade is valid. Execute the trade
			//Get the trade item stack
			ItemStack giveStack = trade.getSellItem();
			//Give the trade item
			data.putItem(giveStack);

			//Log the successful trade
			this.getLogger().AddLog(data.getPlayerSource(), trade, price, this.getCoreSettings().isCreative());
			this.markLoggerDirty();

			//Push the post-trade event
			this.runPostTradeEvent(data.getPlayerSource(), tradeIndex, price);

			//Ignore editing internal storage if this is flagged as creative
			if(!this.getCoreSettings().isCreative())
			{
				//Remove the sold items from storage
				trade.RemoveItemsFromStorage(this.getStorage());
				//Give the paid cost to storage
				this.addStoredMoney(price);
				this.markStorageDirty();
			}

			return RemoteTradeResult.SUCCESS;

		}
		//Process a purchase
		else if(trade.isPurchase())
		{
			//Abort if not enough items in the item slots
			if(!data.hasItem(trade.getSellItem()))
				return RemoteTradeResult.FAIL_CANNOT_AFFORD;
			//Abort if not enough room to store the purchased items (unless we're in creative)
			if(!trade.hasSpace(this) && !this.getCoreSettings().isCreative())
				return RemoteTradeResult.FAIL_NO_INPUT_SPACE;
			//Abort if not enough money to pay them back
			if(!trade.hasStock(this) && !this.getCoreSettings().isCreative())
				return RemoteTradeResult.FAIL_OUT_OF_STOCK;

			//Passed the checks. Take the item(s) from the input
			data.collectItem(trade.getSellItem());
			//Give the payment to the purchaser
			data.givePayment(price);

			//Log the successful trade
			this.getLogger().AddLog(data.getPlayerSource(), trade, price, this.getCoreSettings().isCreative());
			this.markLoggerDirty();

			//Push the post-trade event
			this.runPostTradeEvent(data.getPlayerSource(), tradeIndex, price);

			//Ignore editing internal storage if this is flagged as creative.
			if(!this.getCoreSettings().isCreative())
			{
				//Put the item in storage
				InventoryUtil.TryPutItemStack(this.getStorage(), trade.getSellItem());
				//Remove the coins from storage
				this.removeStoredMoney(price);
				this.markStorageDirty();
			}

			return RemoteTradeResult.SUCCESS;

		}
		//Process a barter
		else if(trade.isBarter())
		{
			//Abort if not enough items in the item slots
			if(!data.hasItem(trade.getBarterItem()))
				return RemoteTradeResult.FAIL_CANNOT_AFFORD;
			//Abort if not enough room to store the purchased items (unless we're creative)
			if(!trade.hasSpace(this) && !this.getCoreSettings().isCreative())
				return RemoteTradeResult.FAIL_NO_INPUT_SPACE;
			//Abort if not enough items in inventory
			if(!trade.hasStock(this) && !this.getCoreSettings().isCreative())
				return RemoteTradeResult.FAIL_OUT_OF_STOCK;

			//Passed the checks. Take the item(s) from the input slot
			data.collectItem(trade.getBarterItem());
			//Check if there's room for the new items
			if(!data.putItem(trade.getSellItem()))
			{
				//Abort if no room for the sold item
				//Put the barter item back
				data.putItem(trade.getBarterItem());
				return RemoteTradeResult.FAIL_NO_OUTPUT_SPACE;
			}

			//Log the successful trade
			this.getLogger().AddLog(data.getPlayerSource(), trade, CoinValue.EMPTY, this.getCoreSettings().isCreative());
			this.markLoggerDirty();

			//Push the post-trade event
			this.runPostTradeEvent(data.getPlayerSource(), tradeIndex, price);

			//Ignore editing internal storage if this is flagged as creative.
			if(!this.getCoreSettings().isCreative())
			{
				//Put the item in storage
				InventoryUtil.TryPutItemStack(this.getStorage(), trade.getBarterItem());
				//Remove the item from storage
				trade.RemoveItemsFromStorage(this.getStorage());
				this.markStorageDirty();
			}

			return RemoteTradeResult.SUCCESS;

		}

		return RemoteTradeResult.FAIL_INVALID_TRADE;
	}
	
	@Override
	protected void forceReopen(List<PlayerEntity> users) {
		for(PlayerEntity player : users)
		{
			if(player.openContainer instanceof ItemTraderStorageContainer)
				this.openStorageMenu(player);
			else if(player.openContainer instanceof ItemTraderContainer)
				this.openTradeMenu(player);
		}
	}
	
	@Override
	public void sendSetTradeItemMessage(int tradeIndex, ItemStack sellItem, int slot) {
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTradeItem2(this.traderID, tradeIndex, sellItem, slot));
	}
	
	@Override
	public void sendSetTradePriceMessage(int tradeIndex, CoinValue newPrice, String newCustomName, ItemTradeType newTradeType) {
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetItemPrice2(this.traderID, tradeIndex, newPrice, newCustomName, newTradeType.name()));
	}
	
	@Override
	public void sendTradeRuleUpdateMessage(int tradeIndex, ResourceLocation type, CompoundNBT updateInfo) {
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageUpdateTradeRule2(this.traderID, tradeIndex, type, updateInfo));
	}
	
	@Override
	public CompoundNBT getPersistentData() {
		CompoundNBT data = new CompoundNBT();
		ITradeRuleHandler.savePersistentRuleData(data, this, this.trades);
		this.logger.write(data);
		return data;
	}

	@Override
	public void loadPersistentData(CompoundNBT data) {
		ITradeRuleHandler.readPersistentRuleData(data, this, this.trades);
		this.logger.read(data);
	}

	@Override
	public void loadFromJson(JsonObject json) throws Exception {
		super.loadFromJson(json);

		if(!json.has("Trades"))
			throw new Exception("Item Trader must have a trade list.");
		JsonArray tradeList = json.get("Trades").getAsJsonArray();
		this.trades = new ArrayList<>();
		for(int i = 0; i < tradeList.size() && this.trades.size() < TRADE_LIMIT; ++i)
		{
			try {
				JsonObject tradeData = tradeList.get(i).getAsJsonObject();
				ItemTradeData newTrade = new ItemTradeData();
				//Sell Item
				JsonObject sellItem = tradeData.get("SellItem").getAsJsonObject();
				newTrade.setSellItem(FileUtil.parseItemStack(sellItem));
				//Trade Type
				if(tradeData.has("TradeType"))
					newTrade.setTradeType(ItemTradeData.loadTradeType(tradeData.get("TradeType").getAsString()));
				//Trade Price
				if(tradeData.has("Price"))
				{
					if(newTrade.isBarter())
						LightmansCurrency.LogWarning("Price is being defined for a barter trade. Price will be ignored.");
					else
						newTrade.setCost(CoinValue.Parse(tradeData.get("Price")));
				}
				else if(!newTrade.isBarter())
				{
					LightmansCurrency.LogWarning("Price is not defined on a non-barter trade. Price will be assumed to be free.");
					newTrade.getCost().setFree(true);
				}
				if(tradeData.has("BarterItem"))
				{
					if(newTrade.isBarter())
					{
						JsonObject barterItem = tradeData.get("BarterItem").getAsJsonObject();
						newTrade.setBarterItem(FileUtil.parseItemStack(barterItem));
					}
					else
					{
						LightmansCurrency.LogWarning("BarterItem is being defined for a non-barter trade. Barter item will be ignored.");
					}
				}
				if(tradeData.has("DisplayName"))
				{
					newTrade.setCustomName(tradeData.get("DisplayName").getAsString());
				}
				if(tradeData.has("TradeRules"))
				{
					newTrade.setRules(TradeRule.Parse(tradeData.get("TradeRules").getAsJsonArray()));
				}
				this.trades.add(newTrade);

			} catch(Exception e) { LightmansCurrency.LogError("Error parsing item trade at index " + i, e); }

			if(this.trades.size() <= 0)
				throw new Exception("Trader has no valid trades!");
			
			this.tradeCount = this.trades.size();
			this.storage = new Inventory(this.inventorySize());

		}

		if(json.has("TradeRules"))
		{
			this.tradeRules = TradeRule.Parse(json.get("TradeRules").getAsJsonArray());
		}

	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) {
		super.saveToJson(json);

		JsonArray trades = new JsonArray();
		for(ItemTradeData trade : this.trades) {
			if(trade.isValid())
			{
				JsonObject tradeData = new JsonObject();
				tradeData.addProperty("TradeType", trade.getTradeType().name());
				tradeData.add("SellItem", FileUtil.convertItemStack(trade.getSellItem()));
				if(trade.isSale() || trade.isPurchase())
					tradeData.add("Price", trade.getCost().toJson());
				if(trade.isBarter())
					tradeData.add("BarterItem", FileUtil.convertItemStack(trade.getBarterItem()));
				if(trade.hasCustomName())
					tradeData.addProperty("DisplayName", trade.getCustomName());
				if(trade.getRules().size() > 0)
					tradeData.add("TradeRules", TradeRule.saveRulesToJson(trade.getRules()));

				trades.add(tradeData);
			}
		}
		json.add("Trades", trades);

		if(this.tradeRules.size() > 0)
			json.add("TradeRules", TradeRule.saveRulesToJson(this.tradeRules));

		return json;
	}
	
	@Override
	public boolean canInteractRemotely() { return true; }
	
	@Override
	public void receiveTradeRuleMessage(PlayerEntity player, int index, ResourceLocation ruleType, CompoundNBT updateInfo) {
		if(!this.hasPermission(player, Permissions.EDIT_TRADE_RULES))
		{
			Settings.PermissionWarning(player, "edit trade rule", Permissions.EDIT_TRADE_RULES);
			return;
		}
		if(index >= 0)
		{
			this.getTrade(index).updateRule(ruleType, updateInfo);
			this.markTradesDirty();
		}
		else
		{
			this.updateRule(ruleType, updateInfo);
			this.markRulesDirty();
		}

	}
	
}
