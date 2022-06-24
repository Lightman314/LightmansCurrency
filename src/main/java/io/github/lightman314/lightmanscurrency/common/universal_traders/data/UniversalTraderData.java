package io.github.lightman314.lightmanscurrency.common.universal_traders.data;

import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.base.Function;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton.ITradeData;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.menus.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.logger.MessageClearUniversalLogger;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageChangeSettings2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenStorage2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenTrades2;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkHooks;

public abstract class UniversalTraderData implements ITrader{
	
	public static final ResourceLocation ICON_RESOURCE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/universal_trader_icons.png");
	
	CoreTraderSettings coreSettings = new CoreTraderSettings(this, this::markCoreSettingsDirty, this::sendSettingsUpdateToServer);
	
	UUID traderID = null;
	public UUID getTraderID() { return this.traderID; }
	public void initTraderID(UUID traderID)
	{
		if(this.traderID == null)
			this.traderID = traderID;
		else
			LightmansCurrency.LogWarning("Attempted to set the traders ID when it's already been defined.");
	}
	
	BlockPos pos = new BlockPos(0,0,0);
	public BlockPos getPos() { return this.pos; }
	ResourceKey<Level> world = Level.OVERWORLD;
	public ResourceKey<Level> getWorld() { return this.world; }
	CoinValue storedMoney = new CoinValue();
	public CoinValue getStoredMoney() {
		if(this.coreSettings.hasBankAccount())
		{
			BankAccount account = this.coreSettings.getBankAccount();
			return account.getCoinStorage().copy();
		}
		return this.storedMoney;
	}
	public CoinValue getInternalStoredMoney() { return this.storedMoney; }
	
	private boolean isServer = true;
	public final boolean isServer() { return this.isServer; }
	public final boolean isClient() { return !this.isServer; }
	public final UniversalTraderData flagAsClient() { this.isServer = false; return this; }
	
	public boolean hasValidTrade() { return !this.hasNoValidTrades(); }
	public boolean hasNoValidTrades() {
		for(ITradeData trade : this.getTradeInfo())
		{
			if(trade.isValid())
				return false;
		}
		return true;
	}
	
	@Override
	public TraderCategory getNotificationCategory() {
		ItemLike icon = ModItems.TRADING_CORE.get();
		try {
			icon = getCategoryItem();
		} catch(Exception e) {}
		return new TraderCategory(icon, this.getName());
	}
	
	protected abstract ItemLike getCategoryItem();
	
	/**
	 * Sends an update for this traders data with a fresh data write of this traders data. Should be used sparingly.
	 * Also marks the trading office dirty so that it will be saved to file.
	 */
	@Deprecated
	public final void markDirty()
	{
		if(this.isServer)
			TradingOffice.MarkDirty(this.traderID);
	}
	
	/**
	 * Sends an update for this traders data informing them of the changes made to this universal trader included in given compound data.
	 * Also marks the trading office dirty so that it will be saved to file.
	 * Core data is added to the nbt automatically.
	 */
	public final void markDirty(CompoundTag compound)
	{
		if(this.isServer)
		{
			//Add the core data to the data list
			this.writeCoreData(compound);
			//Mark as dirty with custom message
			TradingOffice.MarkDirty(this.traderID, compound);
		}
	}
	
	/**
	 * Sends an update for this traders data informing them of the changes made to this universal trader included in given compound data.
	 * Also marks the trading office dirty so that it will be saved to file.
	 * Core data is added to the nbt automatically.
	 */
	public final void markDirty(Function<CompoundTag,CompoundTag> writer) { if(this.isServer) this.markDirty(writer.apply(new CompoundTag())); }
	
	public CoreTraderSettings getCoreSettings() { return this.coreSettings; }
	
	public void markCoreSettingsDirty() {
		this.markDirty(this::writeCoreSettings);
	}
	
	protected final void sendSettingsUpdateToServer(ResourceLocation type, CompoundTag updateInfo)
	{
		if(this.isClient())
		{
			//LightmansCurrency.LogInfo("Sending settings update packet from client to server.\n" + compound.toString());
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageChangeSettings2(this.traderID, type, updateInfo));
		}
	}
	
	public final void changeSettings(ResourceLocation type, Player requestor, CompoundTag updateInfo)
	{
		if(this.isClient())
			LightmansCurrency.LogError("UniversalTraderData.changeSettings was called on a client.");
		if(type.equals(this.coreSettings.getType()))
			this.coreSettings.changeSetting(requestor, updateInfo);
		else
		{
			this.getAdditionalSettings().forEach(setting ->{
				if(type.equals(setting.getType()))
					setting.changeSetting(requestor, updateInfo);
			});
		}
	}
	
	public void addStoredMoney(CoinValue addedAmount)
	{
		if(this.coreSettings.hasBankAccount())
		{
			BankAccount account = this.coreSettings.getBankAccount();
			account.depositCoins(addedAmount);
			account.LogInteraction(this, addedAmount, true);
			return;
		}
		this.storedMoney.addValue(addedAmount);
		this.markDirty(this::writeStoredMoney);
	}
	
	public void removeStoredMoney(CoinValue removedAmount)
	{
		if(this.coreSettings.hasBankAccount())
		{
			BankAccount account = this.coreSettings.getBankAccount();
			account.withdrawCoins(removedAmount);
			account.LogInteraction(this, removedAmount, false);
			return;
		}
		long newValue = this.storedMoney.getRawValue() - removedAmount.getRawValue();
		this.storedMoney.readFromOldValue(newValue);
		this.markDirty(this::writeStoredMoney);
	}
	
	public void clearStoredMoney()
	{
		this.storedMoney = new CoinValue();
		this.markDirty(this::writeStoredMoney);
	}
	
	public void markMoneyDirty() {
		this.markDirty(this::writeStoredMoney);
	}
	
	public UniversalTraderData() {};
	
	public UniversalTraderData(PlayerReference owner, BlockPos pos, ResourceKey<Level> world, UUID traderID)
	{
		this.coreSettings.initializeOwner(owner);
		this.pos = pos;
		this.traderID = traderID;
		this.world = world;
	}
	
	public void read(CompoundTag compound)
	{
		this.read(compound, true);
	}
	
	private void read(CompoundTag compound, boolean checkVersion)
	{
		//ID
		if(compound.contains("ID"))
			this.traderID = compound.getUUID("ID");
		//Core Settings
		if(compound.contains("CoreSettings", Tag.TAG_COMPOUND))
			this.coreSettings.load(compound.getCompound("CoreSettings"));
		else
			this.coreSettings.loadFromOldUniversalData(compound);
		//Position
		if(compound.contains("x") && compound.contains("y") && compound.contains("z"))
			this.pos = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
		if(compound.contains("World"))
			this.world = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString("World")));
		//Stored Money
		if(compound.contains("StoredMoney"))
			this.storedMoney.readFromNBT(compound, "StoredMoney");
		
		//Read Version
		if(checkVersion)
			this.readVersion(compound);
	}
	
	public CompoundTag write(CompoundTag compound)
	{
		this.writeCoreData(compound);
		this.writeCoreSettings(compound);
		this.writeStoredMoney(compound);
		this.writeWorldData(compound);
		this.writeVersion(compound);
		return compound;
	}
	
	protected final CompoundTag writeCoreData(CompoundTag compound)
	{
		//Trader ID
		if(this.traderID != null)
			compound.putUUID("ID", this.traderID);
		//Deserializer Type
		compound.putString("type", getTraderType().toString());
		return compound;
	}
	
	protected final CompoundTag writeCoreSettings(CompoundTag compound)
	{
		compound.put("CoreSettings", this.coreSettings.save(new CompoundTag()));
		return compound;
	}
	
	protected final CompoundTag writeWorldData(CompoundTag compound)
	{
		if(this.pos != null)
		{
			compound.putInt("x", this.pos.getX());
			compound.putInt("y", this.pos.getY());
			compound.putInt("z", this.pos.getZ());
		}
		if(this.world != null)
			compound.putString("World", this.world.location().toString());
		return compound;
	}
	
	protected final CompoundTag writeStoredMoney(CompoundTag compound)
	{
		this.storedMoney.writeToNBT(compound, "StoredMoney");
		return compound;
	}
	
	protected final CompoundTag writeVersion(CompoundTag compound)
	{
		compound.putInt("TraderVersion", this.GetCurrentVersion());
		return compound;
	}
	
	protected final void readVersion(CompoundTag compound)
	{
		//Version Validation
		if(compound.contains("TraderVersion", Tag.TAG_INT))
		{
			int oldVersion = compound.getInt("TraderVersion");
			if(oldVersion < this.GetCurrentVersion())
				this.onVersionUpdate(oldVersion);
		}
	}
	
	protected abstract void onVersionUpdate(int oldVersion);
	
	public int GetCurrentVersion() { return 0; }
	
	public abstract ResourceLocation getTraderType();
	
	protected MenuProvider getTradeMenuProvider() { return new TradeMenuProvider(this.traderID); }
	
	public void openTradeMenu(Player playerEntity)
	{
		MenuProvider provider = getTradeMenuProvider();
		if(provider == null)
		{
			LightmansCurrency.LogError("No trade container provider was given for the universal trader of type " + this.getTraderType().toString());
			return;
		}
		if(playerEntity instanceof ServerPlayer)
			NetworkHooks.openGui((ServerPlayer)playerEntity, provider, new DataWriter(this.getTraderID()));
		else
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
	}
	
	public static class TradeMenuProvider implements MenuProvider
	{
		private final UUID traderID;
		public TradeMenuProvider(UUID traderID) { this.traderID = traderID; }
		@Override
		public Component getDisplayName() { return Component.empty(); }
		@Override
		public AbstractContainerMenu createMenu(int windowID, Inventory inventory, Player player) { return new TraderMenu.TraderMenuUniversal(windowID, inventory, this.traderID); }
	}
	
	protected MenuProvider getStorageMenuProvider() { return new StorageMenuProvider(this.traderID); }
	
	public void openStorageMenu(Player player)
	{
		if(!this.hasPermission(player, Permissions.OPEN_STORAGE))
		{
			Settings.PermissionWarning(player, "open trader storage", Permissions.OPEN_STORAGE);
			return;
		}
		MenuProvider provider = getStorageMenuProvider();
		if(provider == null)
		{
			LightmansCurrency.LogError("No storage container provider was given for the universal trader of type " + this.getTraderType().toString());
			return;
		}
		if(player instanceof ServerPlayer)
			NetworkHooks.openGui((ServerPlayer)player, provider, new DataWriter(this.getTraderID()));
		else
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
	}
	
	public static class StorageMenuProvider implements MenuProvider
	{
		private final UUID traderID;
		public StorageMenuProvider(UUID traderID) { this.traderID = traderID; }
		@Override
		public Component getDisplayName() { return Component.empty(); }
		@Override
		public AbstractContainerMenu createMenu(int windowID, Inventory inventory, Player player) { return new TraderStorageMenu.TraderStorageMenuUniversal(windowID, inventory, this.traderID); }
	}
	
	public MutableComponent getDefaultName()
	{
		return Component.translatable("gui.lightmanscurrency.universaltrader.default");
	}
	
	protected class DataWriter implements Consumer<FriendlyByteBuf>
	{
		final UUID traderID;
		public DataWriter(UUID traderID) { this.traderID = traderID; }
		@Override
		public void accept(FriendlyByteBuf buffer) { buffer.writeUUID(this.traderID); }
	}
	
	protected class TradeIndexDataWriter implements Consumer<FriendlyByteBuf>
	{
		final UUID traderID;
		final int tradeIndex;
		public TradeIndexDataWriter(UUID traderID, int tradeIndex) { this.traderID = traderID; this.tradeIndex = tradeIndex; }
		@Override
		public void accept(FriendlyByteBuf buffer) { buffer.writeUUID(this.traderID); buffer.writeInt(this.tradeIndex); }
	}
	
	public static boolean equals(UniversalTraderData data1, UniversalTraderData data2)
	{
		return data1.write(new CompoundTag()).equals(data2.write(new CompoundTag()));
	}
	
	public abstract IconData getIcon();
	
	@Override
	public void sendOpenTraderMessage() {
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades2(this.traderID));
	}

	@Override
	public void sendOpenStorageMessage() {
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage2(this.traderID));
	}

	@Override
	public void sendClearLogMessage() {
		if(this.isClient())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageClearUniversalLogger(this.traderID));
	}
	
	/**
	 * Adds persistent data, such as trade rules tracking player limitations
	 */
	public abstract CompoundTag getPersistentData();
	/**
	 * Loads persistent data
	 */
	public abstract void loadPersistentData(CompoundTag data);
	
	/**
	 * Safely loads the trader from JSON data.
	 * Commonly used to load persistent traders from the persistentTraders.json file.
	 */
	public void loadFromJson(JsonObject json) throws Exception {
		if(json.has("TraderName"))
			this.coreSettings.forceCustomName(json.get("TraderName").getAsString());
		if(json.has("OwnerName"))
			this.coreSettings.setCustomOwnerName(json.get("OwnerName").getAsString());
	}
	
	/**
	 * Saves the trader data to json so it can be more easily added to the persistentTraders.json file.
	 */
	public JsonObject saveToJson(JsonObject json) {
		json.addProperty("id", "ExampleID");
		json.addProperty("type", this.getTraderType().toString());
		if(this.coreSettings.hasCustomName())
			json.addProperty("TraderName", this.coreSettings.getCustomName());
		json.addProperty("OwnerName", "Minecraft");
		return json;
	}
	
	public void onRemoved() {}
	
	/**
	 * Whether the data should be removed due to the server block being destroyed via unauthorized means.
	 */
	public boolean shouldRemove(MinecraftServer server) {
		try {
			BlockPos pos = this.pos;
			ServerLevel world = server.getLevel(this.world);
			if(world.isLoaded(pos))
			{
				BlockEntity blockEntity = world.getBlockEntity(pos);
				if(blockEntity instanceof UniversalTraderBlockEntity)
				{
					UniversalTraderBlockEntity traderEntity = (UniversalTraderBlockEntity)blockEntity;
					return traderEntity.getTraderID() == null || !traderEntity.getTraderID().equals(this.traderID);
				}
				return true;
			}
		} catch(Exception e) {}
		return false;
	}
	
}
