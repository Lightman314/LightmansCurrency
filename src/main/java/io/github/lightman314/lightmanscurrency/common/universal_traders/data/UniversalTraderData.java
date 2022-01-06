package io.github.lightman314.lightmanscurrency.common.universal_traders.data;

import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.base.Function;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageChangeSettings2;
import io.github.lightman314.lightmanscurrency.tileentity.IPermissions;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class UniversalTraderData implements IPermissions, ITrader{
	
	public static final ResourceLocation ICON_RESOURCE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/universal_trader_icons.png");
	
	CoreTraderSettings coreSettings = new CoreTraderSettings(this::markCoreSettingsDirty, this::sendSettingsUpdateToServer);
	
	UUID traderID = null;
	public UUID getTraderID() { return this.traderID; }
	BlockPos pos;
	public BlockPos getPos() { return this.pos; }
	RegistryKey<World> world = World.OVERWORLD;
	public RegistryKey<World> getWorld() { return this.world; }
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
	public final void markDirty(CompoundNBT compound)
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
	public final void markDirty(Function<CompoundNBT,CompoundNBT> writer) { if(this.isServer) this.markDirty(writer.apply(new CompoundNBT())); }
	
	public CoreTraderSettings getCoreSettings() { return this.coreSettings; }
	
	public void markCoreSettingsDirty() {
		this.markDirty(this::writeCoreSettings);
	}
	
	protected final void sendSettingsUpdateToServer(ResourceLocation type, CompoundNBT updateInfo)
	{
		if(this.isClient())
		{
			//LightmansCurrency.LogInfo("Sending settings update packet from client to server.\n" + compound.toString());
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageChangeSettings2(this.traderID, type, updateInfo));
		}
	}
	
	public void changeSettings(ResourceLocation type, PlayerEntity requestor, CompoundNBT updateInfo)
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
	
	public UniversalTraderData(PlayerReference owner, BlockPos pos, RegistryKey<World> world, UUID traderID)
	{
		this.coreSettings.initializeOwner(owner);
		this.pos = pos;
		this.traderID = traderID;
		this.world = world;
	}
	
	@Deprecated //Use empty constructor and read(CompoundNBT) function to load trader data from nbt
	public UniversalTraderData(CompoundNBT compound) { this.read(compound, false); }
	
	public void read(CompoundNBT compound)
	{
		this.read(compound, true);
	}
	
	private void read(CompoundNBT compound, boolean checkVersion)
	{
		//ID
		if(compound.contains("ID"))
			this.traderID = compound.getUniqueId("ID");
		//Core Settings
		if(compound.contains("CoreSettings", Constants.NBT.TAG_COMPOUND))
			this.coreSettings.load(compound.getCompound("CoreSettings"));
		else if(compound.contains("OwnerID"))
			this.coreSettings.loadFromOldUniversalData(compound);
		//Position
		if(compound.contains("x") && compound.contains("y") && compound.contains("z"))
			this.pos = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
		if(compound.contains("World"))
			this.world = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(compound.getString("World")));
		//Stored Money
		if(compound.contains("StoredMoney"))
			this.storedMoney.readFromNBT(compound, "StoredMoney");
		
		//Read Version
		if(checkVersion)
			this.readVersion(compound);
	}
	
	public CompoundNBT write(CompoundNBT compound)
	{
		this.writeCoreData(compound);
		this.writeCoreSettings(compound);
		this.writeStoredMoney(compound);
		this.writeWorldData(compound);
		this.writeVersion(compound);
		return compound;
	}
	
	protected final CompoundNBT writeCoreData(CompoundNBT compound)
	{
		//Trader ID
		if(this.traderID != null)
			compound.putUniqueId("ID", this.traderID);
		//Deserializer Type
		compound.putString("type", getTraderType().toString());
		return compound;
	}
	
	protected final CompoundNBT writeCoreSettings(CompoundNBT compound)
	{
		compound.put("CoreSettings", this.coreSettings.save(new CompoundNBT()));
		return compound;
	}
	
	protected final CompoundNBT writeWorldData(CompoundNBT compound)
	{
		if(this.pos != null)
		{
			compound.putInt("x", this.pos.getX());
			compound.putInt("y", this.pos.getY());
			compound.putInt("z", this.pos.getZ());
		}
		if(this.world != null)
			compound.putString("World", this.world.getLocation().toString());
		return compound;
	}
	
	protected final CompoundNBT writeStoredMoney(CompoundNBT compound)
	{
		this.storedMoney.writeToNBT(compound, "StoredMoney");
		return compound;
	}
	
	protected final CompoundNBT writeVersion(CompoundNBT compound)
	{
		compound.putInt("TraderVersion", this.GetCurrentVersion());
		return compound;
	}
	
	protected final void readVersion(CompoundNBT compound)
	{
		//Version Validation
		if(compound.contains("TraderVersion", Constants.NBT.TAG_INT))
		{
			int oldVersion = compound.getInt("TraderVersion");
			if(oldVersion < this.GetCurrentVersion())
				this.onVersionUpdate(oldVersion);
		}
	}
	
	public boolean hasPermission(PlayerEntity player, String permission)
	{
		return this.coreSettings.hasPermission(player, permission);
	}
	
	public int getPermissionLevel(PlayerEntity player, String permission)
	{
		return this.coreSettings.getPermissionLevel(player, permission);
	}
	
	protected abstract void onVersionUpdate(int oldVersion);
	
	public int GetCurrentVersion() { return 0; }
	
	public abstract ResourceLocation getTraderType();
	
	protected abstract INamedContainerProvider getTradeMenuProvider();
	
	public void openTradeMenu(PlayerEntity playerEntity)
	{
		INamedContainerProvider provider = getTradeMenuProvider();
		if(provider == null)
		{
			LightmansCurrency.LogError("No trade container provider was given for the universal trader of type " + this.getTraderType().toString());
			return;
		}
		if(playerEntity instanceof ServerPlayerEntity)
			NetworkHooks.openGui((ServerPlayerEntity)playerEntity, provider, new DataWriter(this.getTraderID()));
		else
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
	}
	
	protected abstract INamedContainerProvider getStorageMenuProvider();
	
	public void openStorageMenu(PlayerEntity player)
	{
		if(!this.hasPermission(player, Permissions.OPEN_STORAGE))
		{
			Settings.PermissionWarning(player, "open trader storage", Permissions.OPEN_STORAGE);
			return;
		}
		
		INamedContainerProvider provider = getStorageMenuProvider();
		if(provider == null)
		{
			LightmansCurrency.LogError("No storage container provider was given for the universal trader of type " + this.getTraderType().toString());
			return;
		}
		if(player instanceof ServerPlayerEntity)
			NetworkHooks.openGui((ServerPlayerEntity)player, provider, new DataWriter(this.getTraderID()));
		else
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
	}
	
	protected ITextComponent getDefaultName()
	{
		return new TranslationTextComponent("gui.lightmanscurrency.universaltrader.default");
	}
	
	public ITextComponent getName()
	{
		if(this.coreSettings.hasCustomName())
			return new StringTextComponent(this.coreSettings.getCustomName());
		return getDefaultName();
	}
	
	public ITextComponent getTitle()
	{
		if(this.getCoreSettings().isCreative())
			return this.getName();
		return new TranslationTextComponent("gui.lightmanscurrency.trading.title", this.getName(), this.coreSettings.getOwner().lastKnownName());
	}
	
	protected class DataWriter implements Consumer<PacketBuffer>
	{
		final UUID traderID;
		public DataWriter(UUID traderID) { this.traderID = traderID; }
		@Override
		public void accept(PacketBuffer buffer) { buffer.writeUniqueId(this.traderID); }
	}
	
	protected class TradeIndexDataWriter implements Consumer<PacketBuffer>
	{
		final UUID traderID;
		final int tradeIndex;
		public TradeIndexDataWriter(UUID traderID, int tradeIndex) { this.traderID = traderID; this.tradeIndex = tradeIndex; }
		@Override
		public void accept(PacketBuffer buffer) { buffer.writeUniqueId(this.traderID); buffer.writeInt(this.tradeIndex); }
	}
	
	public static boolean equals(UniversalTraderData data1, UniversalTraderData data2)
	{
		return data1.write(new CompoundNBT()).equals(data2.write(new CompoundNBT()));
	}
	
	public abstract ResourceLocation IconLocation();
	
	public abstract int IconPositionX();
	
	public abstract int IconPositionY();
	
}
