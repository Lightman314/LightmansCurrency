package io.github.lightman314.lightmanscurrency.common.universal_traders.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.base.Function;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blockentity.interfaces.IPermissions;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

public abstract class UniversalTraderData implements IPermissions, ITrader{
	
	public static final ResourceLocation ICON_RESOURCE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/universal_trader_icons.png");
	
	UUID traderID = null;
	public UUID getTraderID() { return this.traderID; }
	UUID ownerID;
	public UUID getOwnerID() { return this.ownerID; }
	String ownerName;
	public String getOwnerName() { return this.ownerName; }
	String traderName = "";
	BlockPos pos;
	public BlockPos getPos() { return this.pos; }
	ResourceKey<Level> world = Level.OVERWORLD;
	public ResourceKey<Level> getWorld() { return this.world; }
	boolean creative = false;
	public boolean isCreative() { return this.creative; }
	public void toggleCreative() { this.creative = !this.creative; this.markDirty(); LightmansCurrency.LogInfo("Creative has been toggled on a Universal Trader.");}
	CoinValue storedMoney = new CoinValue();
	public CoinValue getStoredMoney() { return this.storedMoney; }
	
	List<String> allies = new ArrayList<>();
	
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
	
	public UniversalTraderData(UUID ownerID, String ownerName, BlockPos pos, ResourceKey<Level> world, UUID traderID)
	{
		this.ownerID = ownerID;
		this.ownerName = ownerName;
		this.traderName = "";
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
		//Owner ID & Name
		if(compound.contains("OwnerID"))
			this.ownerID = compound.getUUID("OwnerID");
		if(compound.contains("OwnerName", Tag.TAG_STRING))
			this.ownerName = compound.getString("OwnerName");
		//Trader Name
		if(compound.contains("TraderName", Tag.TAG_STRING))
			this.traderName = compound.getString("TraderName");
		//Position
		if(compound.contains("x") && compound.contains("y") && compound.contains("z"))
			this.pos = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
		if(compound.contains("World"))
			this.world = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString("World")));
		//Creative
		if(compound.contains("Creative"))
			this.creative = compound.getBoolean("Creative");
		//Stored Money
		if(compound.contains("StoredMoney"))
			this.storedMoney.readFromNBT(compound, "StoredMoney");
		
		//Read allies
		if(compound.contains("Allies",Tag.TAG_LIST))
		{
			this.allies.clear();
			ListTag allyList = compound.getList("Allies", Tag.TAG_COMPOUND);
			for(int i = 0; i < allyList.size(); i++)
			{
				CompoundTag thisAlly = allyList.getCompound(i);
				if(thisAlly.contains("name", Tag.TAG_STRING))
					this.allies.add(thisAlly.getString("name"));
			}
		}
		
		//Read Version
		if(checkVersion)
			this.readVersion(compound);
	}
	
	public CompoundTag write(CompoundTag compound)
	{
		this.writeCoreData(compound);
		this.writeOwner(compound);
		this.writeStoredMoney(compound);
		this.writeName(compound);
		this.writeCreative(compound);
		this.writeWorldData(compound);
		this.writeAllies(compound);
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
	
	protected final CompoundTag writeOwner(CompoundTag compound)
	{
		if(this.ownerID != null)
			compound.putUUID("OwnerID", this.ownerID);
		compound.putString("OwnerName", this.ownerName);
		return compound;
	}
	
	protected final CompoundTag writeName(CompoundTag compound)
	{
		compound.putString("TraderName", this.traderName);
		return compound;
	}
	
	protected final CompoundTag writeCreative(CompoundTag compound)
	{
		compound.putBoolean("Creative", this.creative);
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
	
	protected final CompoundTag writeAllies(CompoundTag compound)
	{
		//Allies
		ListTag allyList = new ListTag();
		this.allies.forEach(ally ->{
			CompoundTag thisAlly = new CompoundTag();
			thisAlly.putString("name", ally);
			allyList.add(thisAlly);
		});
		compound.put("Allies", allyList);
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
	
	public boolean isOwner(Player player)
	{
		if(this.ownerID != null)
			return player.getUUID().equals(this.ownerID) || TradingOffice.isAdminPlayer(player);
		LightmansCurrency.LogError("Owner ID for the universal trading machine is null. Unable to determine if the owner is valid.");
		return true;
	}
	
	public boolean hasPermissions(Player player)
	{
		return isOwner(player) || this.allies.contains(player.getName().getString());
	}
	
	public List<String> getAllies()
	{
		return this.allies;
	}
	
	public void markAlliesDirty() { this.markDirty(this::writeAllies); }
	
	public void updateOwnerName(String ownerName)
	{
		if(this.ownerName.equals(ownerName))
			return;
		this.ownerName = ownerName;
		this.markDirty(this::writeOwner);
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
	
	public void openStorageMenu(Player playerEntity)
	{
		MenuProvider provider = getStorageMenuProvider();
		if(provider == null)
		{
			LightmansCurrency.LogError("No storage container provider was given for the universal trader of type " + this.getTraderType().toString());
			return;
		}
		if(playerEntity instanceof ServerPlayer)
			NetworkHooks.openGui((ServerPlayer)playerEntity, provider, new DataWriter(this.getTraderID()));
		else
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
	}
	
	protected Component getDefaultName()
	{
		return new TranslatableComponent("gui.lightmanscurrency.universaltrader.default");
	}
	
	public void setName(String newName)
	{
		this.traderName = newName;
		this.markDirty(this::writeName);
	}
	
	public boolean hasCustomName() { return this.traderName != ""; }
	
	public Component getName()
	{
		if(this.traderName != "")
			return new TextComponent(this.traderName);
		return getDefaultName();
	}
	
	public Component getTitle()
	{
		if(this.creative)
			return this.getName();
		return new TranslatableComponent("gui.lightmanscurrency.trading.title", this.getName(), this.ownerName);
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
