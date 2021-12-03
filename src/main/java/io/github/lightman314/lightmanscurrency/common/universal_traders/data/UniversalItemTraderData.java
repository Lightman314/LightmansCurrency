package io.github.lightman314.lightmanscurrency.common.universal_traders.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.api.ItemShopLogger;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.containers.UniversalContainer;
import io.github.lightman314.lightmanscurrency.containers.UniversalItemEditContainer;
import io.github.lightman314.lightmanscurrency.containers.UniversalItemTraderContainer;
import io.github.lightman314.lightmanscurrency.containers.UniversalItemTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenStorage2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageSetTraderRules2;
import io.github.lightman314.lightmanscurrency.tileentity.ItemTraderTileEntity;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;

public class UniversalItemTraderData extends UniversalTraderData implements IItemTrader, ILoggerSupport<ItemShopLogger>, ITradeRuleHandler{
	
	public static final int TRADELIMIT = ItemTraderTileEntity.TRADELIMIT;
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_trader");
	
	public static final int VERSION = 1;
	
	int tradeCount = 1;
	NonNullList<ItemTradeData> trades = null;
	
	IInventory inventory;
	
	private final ItemShopLogger logger = new ItemShopLogger();
	
	List<TradeRule> tradeRules = new ArrayList<>();
	
	public UniversalItemTraderData() {}
	
	public UniversalItemTraderData(Entity owner, BlockPos pos, RegistryKey<World> world, UUID traderID, int tradeCount)
	{
		super(owner.getUniqueID(), owner.getDisplayName().getString(), pos, world, traderID);
		this.tradeCount = MathUtil.clamp(tradeCount, 1, ItemTraderTileEntity.TRADELIMIT);
		this.trades = ItemTradeData.listOfSize(this.tradeCount);
		this.inventory = new Inventory(this.inventorySize());
	}

	@Override
	public void read(CompoundNBT compound)
	{
		if(compound.contains("TradeLimit", Constants.NBT.TAG_INT))
			this.tradeCount = MathUtil.clamp(compound.getInt("TradeLimit"), 1, ItemTraderTileEntity.TRADELIMIT);
		
		if(compound.contains(ItemTradeData.DEFAULT_KEY, Constants.NBT.TAG_LIST))
			this.trades = ItemTradeData.loadAllData(compound, this.tradeCount);
		
		if(compound.contains("Storage", Constants.NBT.TAG_LIST))
			this.inventory = InventoryUtil.loadAllItems("Storage", compound, this.getTradeCount() * 9);
		
		this.logger.read(compound);
		
		if(compound.contains(TradeRule.DEFAULT_TAG, Constants.NBT.TAG_LIST))
			this.tradeRules = TradeRule.readRules(compound);
		
		super.read(compound);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{

		this.writeTrades(compound);
		this.writeStorage(compound);
		this.writeLogger(compound);
		this.writeRules(compound);
		
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
		InventoryUtil.saveAllItems("Storage", compound, this.inventory);
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
	
	public int getTradeCount()
	{
		return this.tradeCount;
	}
	
	public void addTrade()
	{
		if(this.getTradeCount() >= TRADELIMIT)
			return;
		this.overrideTradeCount(this.tradeCount + 1);
		forceReOpen();
	}
	
	public void removeTrade()
	{
		if(this.getTradeCount() <= 1)
			return;
		this.overrideTradeCount(this.tradeCount - 1);
		forceReOpen();
	}
	
	private void forceReOpen()
	{
		UniversalContainer.onForceReopen(this.traderID);
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
		IInventory oldInventory = this.inventory;
		this.inventory = new Inventory(this.inventorySize());
		for(int i = 0; i < this.inventory.getSizeInventory() && i < oldInventory.getSizeInventory(); i++)
		{
			this.inventory.setInventorySlotContents(i, oldInventory.getStackInSlot(i));
		}
		//Attempt to place lost items into the available slots
		if(oldInventory.getSizeInventory() > this.inventorySize())
		{
			for(int i = this.inventorySize(); i < oldInventory.getSizeInventory(); i++)
			{
				InventoryUtil.TryPutItemStack(this.inventory, oldInventory.getStackInSlot(i));
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
	
	public NonNullList<ItemTradeData> getAllTrades()
	{
		return this.trades;
	}
	
	public void markTradesDirty()
	{
		//Send update to the client with only the trade data.
		this.markDirty(this::writeTrades);
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
		return this.inventory;
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
			return new UniversalItemTraderContainer(menuID, inventory, this.traderID);
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
			return new UniversalItemTraderStorageContainer(menuID, inventory, this.traderID);
		}
		@Override
		public ITextComponent getDisplayName() { return new StringTextComponent(""); }
	}
	
	private static class ItemEditProvider implements INamedContainerProvider
	{
		final UUID traderID;
		final int tradeIndex;
		private ItemEditProvider(UUID traderID, int tradeIndex) { this.traderID = traderID; this.tradeIndex = tradeIndex; }

		private UniversalItemTraderData getData()
		{
			UniversalTraderData data = TradingOffice.getData(this.traderID);
			if(data instanceof UniversalItemTraderData)
				return (UniversalItemTraderData)data;
			return null;
		}
		
		@Override
		public Container createMenu(int menuID, PlayerInventory inventory, PlayerEntity player) {
			return new UniversalItemEditContainer(menuID, inventory, () -> getData(), this.tradeIndex);
		}

		@Override
		public ITextComponent getDisplayName() {
			return new StringTextComponent("");
		}
	}
	
	@Override
	public ResourceLocation IconLocation() {
		return UniversalTraderData.ICON_RESOURCE;
	}

	@Override
	public int IconPositionX() {
		return 0;
	}

	@Override
	public int IconPositionY() {
		return 0;
	}
	
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
	
	public ITradeRuleScreenHandler GetRuleScreenHandler() { return new TradeRuleScreenHandler(this); }
	
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
		
	}
	
}
