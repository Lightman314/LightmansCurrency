package io.github.lightman314.lightmanscurrency.common.universal_traders.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ItemShopLogger;
import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.handler.TraderItemHandler;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.RemoteTradeData;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.RemoteTradeData.RemoteTradeResult;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageAddOrRemoveTrade2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenStorage2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageSetItemPrice2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageSetTradeItem2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageSetTraderRules2;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.ItemTraderSettings;
import io.github.lightman314.lightmanscurrency.trader.settings.ItemTraderSettings.ItemHandlerSettings;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData.ItemTradeType;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.menus.ItemTraderMenu.ItemTraderMenuUniversal;
import io.github.lightman314.lightmanscurrency.menus.ItemEditMenu;
import io.github.lightman314.lightmanscurrency.menus.ItemTraderMenu;
import io.github.lightman314.lightmanscurrency.menus.ItemTraderStorageMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;

public class UniversalItemTraderData extends UniversalTraderData implements IItemTrader {
	
	public static final int TRADELIMIT = ItemTraderBlockEntity.TRADELIMIT;
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_trader");
	
	public static final int VERSION = 1;
	
	TraderItemHandler itemHandler = new TraderItemHandler(this);
	
	public IItemHandler getItemHandler(Direction relativeSide)
	{
		ItemHandlerSettings handlerSetting = this.itemSettings.getHandlerSetting(relativeSide);
		return this.itemHandler.getHandler(handlerSetting);
	}
	
	private ItemTraderSettings itemSettings = new ItemTraderSettings(this, this::markItemSettingsDirty, this::sendSettingsUpdateToServer);
	
	int tradeCount = 1;
	NonNullList<ItemTradeData> trades = null;
	
	Container inventory;
	
	private final ItemShopLogger logger = new ItemShopLogger();
	
	List<TradeRule> tradeRules = new ArrayList<>();
	
	public UniversalItemTraderData() {}
	
	public UniversalItemTraderData(PlayerReference owner, BlockPos pos, ResourceKey<Level> world, UUID traderID, int tradeCount)
	{
		super(owner, pos, world, traderID);
		this.tradeCount = MathUtil.clamp(tradeCount, 1, ItemTraderBlockEntity.TRADELIMIT);
		this.trades = ItemTradeData.listOfSize(this.tradeCount);
		this.inventory = new SimpleContainer(this.inventorySize());
	}

	@Override
	public void read(CompoundTag compound)
	{
		if(compound.contains("TradeLimit", Tag.TAG_INT))
			this.tradeCount = MathUtil.clamp(compound.getInt("TradeLimit"), 1, ItemTraderBlockEntity.TRADELIMIT);
		
		if(compound.contains(ItemTradeData.DEFAULT_KEY, Tag.TAG_LIST))
			this.trades = ItemTradeData.loadAllData(compound, this.tradeCount);
		
		if(compound.contains("Storage", Tag.TAG_LIST))
			this.inventory = InventoryUtil.loadAllItems("Storage", compound, this.getTradeCount() * 9);
		
		this.logger.read(compound);
		
		if(compound.contains(TradeRule.DEFAULT_TAG, Tag.TAG_LIST))
			this.tradeRules = TradeRule.readRules(compound);
		
		if(compound.contains("ItemSettings", Tag.TAG_COMPOUND))
			this.itemSettings.load(compound.getCompound("ItemSettings"));
		
		super.read(compound);
	}
	
	@Override
	public CompoundTag write(CompoundTag compound)
	{

		this.writeTrades(compound);
		this.writeStorage(compound);
		this.writeLogger(compound);
		this.writeRules(compound);
		this.writeItemSettings(compound);
		
		return super.write(compound);
		
	}
	
	protected final CompoundTag writeTrades(CompoundTag compound)
	{
		compound.putInt("TradeLimit", this.trades.size());
		ItemTradeData.saveAllData(compound, trades);
		return compound;
	}
	
	protected final CompoundTag writeStorage(CompoundTag compound)
	{
		InventoryUtil.saveAllItems("Storage", compound, this.inventory);
		return compound;
	}
	
	protected final CompoundTag writeLogger(CompoundTag compound)
	{
		this.logger.write(compound);
		return compound;
	}
	
	protected final CompoundTag writeRules(CompoundTag compound)
	{
		TradeRule.writeRules(compound, this.tradeRules);
		return compound;
	}
	
	protected final CompoundTag writeItemSettings(CompoundTag compound)
	{
		compound.put("ItemSettings", this.itemSettings.save(new CompoundTag()));
		return compound;
	}
	
	public int getTradeCount()
	{
		return this.tradeCount;
	}
	
	public int getTradeCountLimit() { return TRADELIMIT; }
	
	public void requestAddOrRemoveTrade(boolean isAdd)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade2(this.traderID, isAdd));
	}
	
	public void addTrade(Player requestor)
	{
		if(!TradingOffice.isAdminPlayer(requestor))
		{
			Settings.PermissionWarning(requestor, "add trade slot", Permissions.ADMIN_MODE);
			return;
		}
		if(this.getTradeCount() >= TRADELIMIT)
			return;
		this.overrideTradeCount(this.tradeCount + 1);
		this.forceReopen();
		this.coreSettings.getLogger().LogAddRemoveTrade(requestor, true, this.tradeCount);
		this.markCoreSettingsDirty();
	}
	
	public void removeTrade(Player requestor)
	{
		if(!TradingOffice.isAdminPlayer(requestor))
		{
			Settings.PermissionWarning(requestor, "remove trade slot", Permissions.ADMIN_MODE);
			return;
		}
		if(this.getTradeCount() <= 1)
			return;
		this.overrideTradeCount(this.tradeCount - 1);
		this.forceReopen();
		this.coreSettings.getLogger().LogAddRemoveTrade(requestor, true, this.tradeCount);
		this.markCoreSettingsDirty();
	}
	
	public void overrideTradeCount(int newTradeCount)
	{
		if(this.tradeCount == newTradeCount)
			return;
		this.tradeCount = MathUtil.clamp(newTradeCount, 1, TRADELIMIT);
		NonNullList<ItemTradeData> oldTrades = this.trades;
		this.trades = ItemTradeData.listOfSize(getTradeCount());
		//Write the old trade data into the array.
		for(int i = 0; i < oldTrades.size() && i < this.trades.size(); i++)
		{
			this.trades.set(i, oldTrades.get(i));
		}
		//Set the new inventory list
		Container oldInventory = this.inventory;
		this.inventory = new SimpleContainer(this.inventorySize());
		for(int i = 0; i < this.inventory.getContainerSize() && i < oldInventory.getContainerSize(); i++)
		{
			this.inventory.setItem(i, oldInventory.getItem(i));
		}
		//Attempt to place lost items into the available slots
		if(oldInventory.getContainerSize() > this.inventorySize())
		{
			for(int i = this.inventorySize(); i < oldInventory.getContainerSize(); i++)
			{
				InventoryUtil.TryPutItemStack(this.inventory, oldInventory.getItem(i));
			}
		}
		//Mark as dirty (both trades & storage)
		CompoundTag compound = this.writeTrades(new CompoundTag());
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
	
	public NonNullList<ItemTradeData> getAllTrades()
	{
		return this.trades;
	}
	
	public void markTradesDirty()
	{
		//Send update to the client with only the trade data.
		this.markDirty(this::writeTrades);
	}
	
	public ItemTraderSettings getItemSettings() { return this.itemSettings; }
	
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
	
	public Container getStorage()
	{
		return this.inventory;
	}
	
	public void markStorageDirty()
	{
		this.markDirty(this::writeStorage);
	}
	
	@Override
	public ResourceLocation getTraderType() {
		return TYPE;
	}
	
	@Override
	protected Component getDefaultName()
	{
		return new TranslatableComponent("gui.lightmanscurrency.universaltrader.item");
	}

	@Override
	protected MenuProvider getTradeMenuProvider() {
		return new TraderProvider(this.traderID);
	}

	@Override
	protected MenuProvider getStorageMenuProvider() {
		return new StorageProvider(this.traderID);
	}
	
	protected MenuProvider getItemEditMenuProvider(int tradeIndex) { return new ItemEditProvider(this.traderID, tradeIndex); }
	
	public void openItemEditMenu(Player player, int tradeIndex)
	{
		MenuProvider provider = getItemEditMenuProvider(tradeIndex);
		if(provider == null)
		{
			LightmansCurrency.LogError("No storage container provider was given for the universal trader of type " + this.getTraderType().toString());
			return;
		}
		if(player instanceof ServerPlayer)
			NetworkHooks.openGui((ServerPlayer)player, provider, new TradeIndexDataWriter(this.getTraderID(), tradeIndex));
		else
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
	}
	
	private static class TraderProvider implements MenuProvider
	{
		final UUID traderID;
		private TraderProvider(UUID traderID) { this.traderID = traderID; }
		@Override
		public AbstractContainerMenu createMenu(int menuID, Inventory inventory, Player player) {
			return new ItemTraderMenu.ItemTraderMenuUniversal(menuID, inventory, this.traderID);
		}
		@Override
		public Component getDisplayName() { return new TextComponent(""); }
	}
	
	private static class StorageProvider implements MenuProvider
	{
		final UUID traderID;
		private StorageProvider(UUID traderID) { this.traderID = traderID; }
		@Override
		public AbstractContainerMenu createMenu(int menuID, Inventory inventory, Player player) {
			return new ItemTraderStorageMenu.ItemTraderStorageMenuUniversal(menuID, inventory, this.traderID);
		}
		@Override
		public Component getDisplayName() { return new TextComponent(""); }
	}
	
	private static class ItemEditProvider implements MenuProvider
	{
		final UUID traderID;
		final int tradeIndex;
		private ItemEditProvider(UUID traderID, int tradeIndex) { this.traderID = traderID; this.tradeIndex = tradeIndex; }
		
		@Override
		public AbstractContainerMenu createMenu(int menuID, Inventory inventory, Player player) {
			return new ItemEditMenu.UniversalItemEditMenu(menuID, inventory, this.traderID, this.tradeIndex);
		}

		@Override
		public Component getDisplayName() { return new TextComponent(""); }
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
					tradeStack = InventoryUtil.TryPutItemStack(this.inventory, tradeStack);
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
		
		public void updateServer(List<TradeRule> newRules)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTraderRules2(this.trader.traderID, newRules));
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
	protected void forceReopen(List<Player> users) {
		for(Player player : users)
		{
			if(player.containerMenu instanceof ItemTraderStorageMenu)
				this.openStorageMenu(player);
			else if(player.containerMenu instanceof ItemTraderMenuUniversal)
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
	public void sendSetTradeRuleMessage(int tradeIndex, List<TradeRule> newRules) {
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTraderRules2(this.traderID, newRules, tradeIndex));
	}

	
	
}
