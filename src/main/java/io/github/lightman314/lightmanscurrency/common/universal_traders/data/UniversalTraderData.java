package io.github.lightman314.lightmanscurrency.common.universal_traders.data;

import java.util.UUID;
import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public abstract class UniversalTraderData {

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
	
	public void markDirty()
	{
		TradingOffice.MarkDirty(this.traderID);
	}
	
	public void addStoredMoney(CoinValue amount)
	{
		this.storedMoney.addValue(amount);
		this.markDirty();
	}
	
	public void removeStoredMoney(CoinValue removedAmount)
	{
		long newValue = this.storedMoney.getRawValue() - removedAmount.getRawValue();
		this.storedMoney.readFromOldValue(newValue);
		this.markDirty();
	}
	
	public void clearStoredMoney()
	{
		this.storedMoney = new CoinValue();
		this.markDirty();
	}
	
	public UniversalTraderData(UUID ownerID, String ownerName, BlockPos pos, ResourceKey<Level> world, UUID traderID)
	{
		this.ownerID = ownerID;
		this.ownerName = ownerName;
		this.traderName = "";
		this.pos = pos;
		this.traderID = traderID;
		this.world = world;
	}
	
	public UniversalTraderData()
	{
		
	}
	
	protected void read(CompoundTag compound)
	{
		//ID
		if(compound.contains("ID"))
			this.traderID = compound.getUUID("ID");
		//Owner ID & Name
		if(compound.contains("OwnerID"))
			this.ownerID = compound.getUUID("OwnerID");
		if(compound.contains("OwnerName", Constants.NBT.TAG_STRING))
			this.ownerName = compound.getString("OwnerName");
		//Trader Name
		if(compound.contains("TraderName", Constants.NBT.TAG_STRING))
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
		
		//Version Validation
		int oldVersion = 0;
		if(compound.contains("TraderVersion", Constants.NBT.TAG_INT))
			oldVersion = compound.getInt("TraderVersion");
		if(oldVersion < this.GetCurrentVersion())
		{
			this.onVersionUpdate(oldVersion);
			this.markDirty();
		}
	}
	
	public boolean isOwner(Player player)
	{
		if(this.creative && player.isCreative() && player.hasPermissions(2))
		{
			return true;
		}
		else if(this.ownerID != null)
			return player.getUUID().equals(this.ownerID);
		//Owner is not defined, so everyone is the owner
		return true;
	}
	
	public void updateOwnerName(String ownerName)
	{
		if(this.ownerName.equals(ownerName))
			return;
		this.ownerName = ownerName;
		this.markDirty();
	}
	
	public CompoundTag write(CompoundTag compound)
	{
		//Trader ID
		if(this.traderID != null)
			compound.putUUID("ID", this.traderID);
		//Deserializer Type
		compound.putString("type", getDeserializerType());
		//Owner ID & Name
		if(this.ownerID != null)
			compound.putUUID("OwnerID", this.ownerID);
		compound.putString("OwnerName", this.ownerName);
		//Trader Custom Name
		compound.putString("TraderName", this.traderName);
		//Creative
		compound.putBoolean("Creative", this.creative);
		//Trader Position
		if(this.pos != null)
		{
			compound.putInt("x", this.pos.getX());
			compound.putInt("y", this.pos.getY());
			compound.putInt("z", this.pos.getZ());
		}
		//Trader World
		if(this.world != null)
			compound.putString("World", this.world.getRegistryName().toString());
		//Stored Money
		this.storedMoney.writeToNBT(compound, "StoredMoney");
		
		compound.putInt("TraderVersion", this.GetCurrentVersion());
		
		return compound;
	}
	
	protected abstract void onVersionUpdate(int oldVersion);
	
	public int GetCurrentVersion() { return 0; }
	
	public abstract String getDeserializerType();
	
	protected abstract MenuProvider getTradeMenuProvider();
	
	public void openTradeMenu(Player playerEntity)
	{
		MenuProvider provider = getTradeMenuProvider();
		if(provider == null)
		{
			LightmansCurrency.LogError("No trade container provider was given for the universal trader of type " + this.getDeserializerType().toString());
			return;
		}
		if(playerEntity instanceof ServerPlayer)
			NetworkHooks.openGui((ServerPlayer)playerEntity, provider, new DataWriter(this.getTraderID(), this.write(new CompoundTag())));
		else
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
	}
	
	protected abstract MenuProvider getStorageMenuProvider();
	
	public void openStorageMenu(Player playerEntity)
	{
		MenuProvider provider = getStorageMenuProvider();
		if(provider == null)
		{
			LightmansCurrency.LogError("No storage container provider was given for the universal trader of type " + this.getDeserializerType().toString());
			return;
		}
		if(playerEntity instanceof ServerPlayer)
			NetworkHooks.openGui((ServerPlayer)playerEntity, provider, new DataWriter(this.getTraderID(), this.write(new CompoundTag())));
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
		this.markDirty();
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
		
		UUID traderID;
		CompoundTag traderCompound;
		
		public DataWriter(UUID traderID, CompoundTag traderCompound)
		{
			this.traderID = traderID;
			this.traderCompound = traderCompound;
		}
		
		@Override
		public void accept(FriendlyByteBuf buffer) {
			buffer.writeUUID(this.traderID);
			buffer.writeNbt(this.traderCompound);
		}
	}
	
	protected class TradeIndexDataWriter implements Consumer<FriendlyByteBuf>
	{
		UUID traderID;
		CompoundTag traderCompound;
		int tradeIndex;
		
		public TradeIndexDataWriter(UUID traderID, CompoundTag traderCompound, int tradeIndex)
		{
			this.traderID = traderID;
			this.traderCompound = traderCompound;
			this.tradeIndex = tradeIndex;
		}
		
		@Override
		public void accept(FriendlyByteBuf buffer) {
			buffer.writeUUID(this.traderID);
			buffer.writeNbt(this.traderCompound);
			buffer.writeInt(this.tradeIndex);
		}
	}
	
	public static boolean equals(UniversalTraderData data1, UniversalTraderData data2)
	{
		return data1.write(new CompoundTag()).equals(data2.write(new CompoundTag()));
	}
	
	public abstract ResourceLocation IconLocation();
	
	public abstract int IconPositionX();
	
	public abstract int IconPositionY();
	
}
