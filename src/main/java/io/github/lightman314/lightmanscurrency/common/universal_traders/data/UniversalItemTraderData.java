package io.github.lightman314.lightmanscurrency.common.universal_traders.data;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ItemShopLogger;
import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.ItemTradeData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces.ITradeButtonStockSource;
import io.github.lightman314.lightmanscurrency.common.universal_traders.IUniversalDataDeserializer;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.containers.UniversalContainer;
import io.github.lightman314.lightmanscurrency.containers.UniversalItemEditContainer;
import io.github.lightman314.lightmanscurrency.containers.UniversalItemTraderContainer;
import io.github.lightman314.lightmanscurrency.containers.UniversalItemTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.interfaces.IItemTrader;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class UniversalItemTraderData extends UniversalTraderData implements ITradeButtonStockSource, IItemTrader{
	
	public static final int TRADELIMIT = ItemTraderBlockEntity.TRADELIMIT;
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_trader");
	public static final Deserializer DESERIALIZER = new Deserializer();
	
	public static final int VERSION = 1;
	
	int tradeCount = 1;
	NonNullList<ItemTradeData> trades = null;
	
	Container inventory;
	
	public final ItemShopLogger logger = new ItemShopLogger();
	
	public UniversalItemTraderData(Entity owner, BlockPos pos, ResourceKey<Level> world, UUID traderID, int tradeCount)
	{
		super(owner.getUUID(), owner.getDisplayName().getString(), pos, world, traderID);
		this.tradeCount = MathUtil.clamp(tradeCount, 1, ItemTraderBlockEntity.TRADELIMIT);
		this.trades = ItemTradeData.listOfSize(this.tradeCount);
		this.inventory = new SimpleContainer(this.inventorySize());
	}

	public UniversalItemTraderData(CompoundTag compound)
	{
		
		if(compound.contains("TradeLimit", Constants.NBT.TAG_INT))
			this.tradeCount = MathUtil.clamp(compound.getInt("TradeLimit"), 1, ItemTraderBlockEntity.TRADELIMIT);
		
		this.trades = ItemTradeData.loadAllData(compound, this.tradeCount);
		
		this.inventory = InventoryUtil.loadAllItems("Storage", compound, this.getTradeCount() * 9);
		
		this.logger.read(compound);
		
		super.read(compound);
		
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
		//Mark as dirty
		this.markDirty();
	}
	
	
	public ItemTradeData getTrade(int tradeIndex)
	{
		if(tradeIndex >= 0 && tradeIndex < getTradeCount())
		{
			return this.trades.get(tradeIndex);
		}
		return new ItemTradeData();
	}
	
	public NonNullList<ItemTradeData> getAllTrades()
	{
		return this.trades;
	}
	
	public void markTradesDirty()
	{
		this.markDirty();
	}
	
	public void markLoggerDirty()
	{
		this.markDirty();
	}
	
	public int inventorySize()
	{
		return this.tradeCount * 9;
	}
	
	public Container getStorage()
	{
		return this.inventory;
	}
	
	@Override
	public CompoundTag write(CompoundTag compound)
	{
		compound.putInt("TradeLimit", this.trades.size());
		ItemTradeData.saveAllData(compound, trades);
		InventoryUtil.saveAllItems("Storage", compound, this.inventory);
		this.logger.write(compound);
		
		return super.write(compound);
	}
	
	@Override
	public String getDeserializerType() {
		return TYPE.toString();
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
			LightmansCurrency.LogError("No storage container provider was given for the universal trader of type " + this.getDeserializerType().toString());
			return;
		}
		if(player instanceof ServerPlayer)
			NetworkHooks.openGui((ServerPlayer)player, provider, new TradeIndexDataWriter(this.getTraderID(), this.write(new CompoundTag()), tradeIndex));
		else
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
	}
	
	private static class Deserializer implements IUniversalDataDeserializer<UniversalItemTraderData>
	{
		@Override
		public UniversalItemTraderData deserialize(CompoundTag compound) {
			return new UniversalItemTraderData(compound);
		}
	}
	
	private static class TraderProvider implements MenuProvider
	{
		final UUID traderID;
		
		private TraderProvider(UUID traderID)
		{
			this.traderID = traderID;
		}

		@Override
		public AbstractContainerMenu createMenu(int menuID, Inventory inventory, Player player) {
			return new UniversalItemTraderContainer(menuID, inventory, this.traderID);
		}

		@Override
		public Component getDisplayName() {
			return new TextComponent("");
		}
		
	}
	
	private static class StorageProvider implements MenuProvider
	{
		final UUID traderID;
		
		private StorageProvider(UUID traderID)
		{
			this.traderID = traderID;
		}

		@Override
		public AbstractContainerMenu createMenu(int menuID, Inventory inventory, Player player) {
			return new UniversalItemTraderStorageContainer(menuID, inventory, this.traderID);
		}

		@Override
		public Component getDisplayName() {
			return new TextComponent("");
		}
		
	}
	
	private static class ItemEditProvider implements MenuProvider
	{
		
		final UUID traderID;
		final int tradeIndex;
		
		private ItemEditProvider(UUID traderID, int tradeIndex)
		{
			this.traderID = traderID;
			this.tradeIndex = tradeIndex;
		}

		private UniversalItemTraderData getData()
		{
			UniversalTraderData data = TradingOffice.getData(this.traderID);
			if(data instanceof UniversalItemTraderData)
				return (UniversalItemTraderData)data;
			return null;
		}
		
		@Override
		public AbstractContainerMenu createMenu(int menuID, Inventory inventory, Player player) {
			return new UniversalItemEditContainer(menuID, inventory, () -> getData(), this.tradeIndex);
		}

		@Override
		public Component getDisplayName() {
			return new TextComponent("");
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

	
	
}
