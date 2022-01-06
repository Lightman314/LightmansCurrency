package io.github.lightman314.lightmanscurrency.common.universal_traders.data;

import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.base.Function;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageChangeSettings2;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public abstract class UniversalTraderData implements ITrader{
	
	public static final ResourceLocation ICON_RESOURCE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/universal_trader_icons.png");
	
	CoreTraderSettings coreSettings = new CoreTraderSettings(this::markCoreSettingsDirty, this::sendSettingsUpdateToServer);
	
	UUID traderID = null;
	public UUID getTraderID() { return this.traderID; }
	
	BlockPos pos;
	public BlockPos getPos() { return this.pos; }
	ResourceKey<Level> world = Level.OVERWORLD;
	public ResourceKey<Level> getWorld() { return this.world; }
	CoinValue storedMoney = new CoinValue();
	public CoinValue getStoredMoney() { return this.storedMoney; }
	
	private boolean isServer = true;
	public final boolean isServer() { return this.isServer; }
	public final boolean isClient() { return !this.isServer; }
	public final UniversalTraderData flagAsClient() { this.isServer = false; return this; }
	
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
	
	public void changeSettings(ResourceLocation type, Player requestor, CompoundTag updateInfo)
	{
		if(this.isClient())
			LightmansCurrency.LogError("UniversalTraderData.changeSettings was called on a client.");
		if(type.equals(this.coreSettings.getType()))
		{
			//LightmansCurrency.LogInfo("Settings change message from update message.");
			this.coreSettings.changeSetting(requestor, updateInfo);
			//Don't need to mark it dirty. The change settings function will mark itself dirty if a change is made.
		}
	}
	
	public void addStoredMoney(CoinValue amount)
	{
		this.storedMoney.addValue(amount);
		this.markDirty(this::writeStoredMoney);
	}
	
	public void removeStoredMoney(CoinValue removedAmount)
	{
		long newValue = this.storedMoney.getRawValue() - removedAmount.getRawValue();
		this.storedMoney.readFromOldValue(newValue);
		this.markDirty(this::writeStoredMoney);
	}
	
	public void clearStoredMoney()
	{
		this.storedMoney = new CoinValue();
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
	
	public int getPermissionLevel(Player player, String permission)
	{
		return this.coreSettings.getPermissionLevel(player, permission);
	}
	
	protected abstract void onVersionUpdate(int oldVersion);
	
	public int GetCurrentVersion() { return 0; }
	
	public abstract ResourceLocation getTraderType();
	
	protected abstract MenuProvider getTradeMenuProvider();
	
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
	
	protected abstract MenuProvider getStorageMenuProvider();
	
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
	
	protected Component getDefaultName()
	{
		return new TranslatableComponent("gui.lightmanscurrency.universaltrader.default");
	}
	
	public Component getName()
	{
		if(this.coreSettings.hasCustomName())
			return new TextComponent(this.coreSettings.getCustomName());
		return getDefaultName();
	}
	
	public Component getTitle()
	{
		if(this.coreSettings.isCreative() || this.coreSettings.getOwner() == null)
			return this.getName();
		return new TranslatableComponent("gui.lightmanscurrency.trading.title", this.getName(), this.coreSettings.getOwner().lastKnownName());
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
	
	public abstract ResourceLocation IconLocation();
	
	public abstract int IconPositionX();
	
	public abstract int IconPositionY();
	
}
