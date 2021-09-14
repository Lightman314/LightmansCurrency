package io.github.lightman314.lightmanscurrency.common.universal_traders.data;

import java.util.UUID;
import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
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
	RegistryKey<World> world = World.OVERWORLD;
	public RegistryKey<World> getWorld() { return this.world; }
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
	
	public UniversalTraderData(UUID ownerID, String ownerName, BlockPos pos, RegistryKey<World> world, UUID traderID)
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
	
	protected void read(CompoundNBT compound)
	{
		//ID
		if(compound.contains("ID"))
			this.traderID = compound.getUniqueId("ID");
		//Owner ID & Name
		if(compound.contains("OwnerID"))
			this.ownerID = compound.getUniqueId("OwnerID");
		if(compound.contains("OwnerName", Constants.NBT.TAG_STRING))
			this.ownerName = compound.getString("OwnerName");
		//Trader Name
		if(compound.contains("TraderName", Constants.NBT.TAG_STRING))
			this.traderName = compound.getString("TraderName");
		//Position
		if(compound.contains("x") && compound.contains("y") && compound.contains("z"))
			this.pos = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
		if(compound.contains("World"))
			this.world = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(compound.getString("World")));
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
			this.onVersionUpdate(oldVersion);
	}
	
	public boolean isOwner(PlayerEntity player)
	{
		if(this.creative && player.isCreative() && player.hasPermissionLevel(2))
		{
			return true;
		}
		else if(this.ownerID != null)
			return player.getUniqueID().equals(this.ownerID);
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
	
	public CompoundNBT write(CompoundNBT compound)
	{
		//Trader ID
		if(this.traderID != null)
			compound.putUniqueId("ID", this.traderID);
		//Deserializer Type
		compound.putString("type", getDeserializerType());
		//Owner ID & Name
		if(this.ownerID != null)
			compound.putUniqueId("OwnerID", this.ownerID);
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
			compound.putString("World", this.world.getLocation().toString());
		//Stored Money
		this.storedMoney.writeToNBT(compound, "StoredMoney");
		
		compound.putInt("TraderVersion", this.GetCurrentVersion());
		
		return compound;
	}
	
	protected abstract void onVersionUpdate(int oldVersion);
	
	public int GetCurrentVersion() { return 0; }
	
	public abstract String getDeserializerType();
	
	protected abstract INamedContainerProvider getTradeMenuProvider();
	
	public void openTradeMenu(PlayerEntity playerEntity)
	{
		INamedContainerProvider provider = getTradeMenuProvider();
		if(provider == null)
		{
			LightmansCurrency.LogError("No trade container provider was given for the universal trader of type " + this.getDeserializerType().toString());
			return;
		}
		if(playerEntity instanceof ServerPlayerEntity)
			NetworkHooks.openGui((ServerPlayerEntity)playerEntity, provider, new DataWriter(this.getTraderID(), this.write(new CompoundNBT())));
		else
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
	}
	
	protected abstract INamedContainerProvider getStorageMenuProvider();
	
	public void openStorageMenu(PlayerEntity playerEntity)
	{
		INamedContainerProvider provider = getStorageMenuProvider();
		if(provider == null)
		{
			LightmansCurrency.LogError("No storage container provider was given for the universal trader of type " + this.getDeserializerType().toString());
			return;
		}
		if(playerEntity instanceof ServerPlayerEntity)
			NetworkHooks.openGui((ServerPlayerEntity)playerEntity, provider, new DataWriter(this.getTraderID(), this.write(new CompoundNBT())));
		else
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
	}
	
	protected ITextComponent getDefaultName()
	{
		return new TranslationTextComponent("gui.lightmanscurrency.universaltrader.default");
	}
	
	public void setName(String newName)
	{
		this.traderName = newName;
	}
	
	public boolean hasCustomName() { return this.traderName != ""; }
	
	public ITextComponent getName()
	{
		if(this.traderName != "")
			return new StringTextComponent(this.traderName);
		return getDefaultName();
	}
	
	public ITextComponent getTitle()
	{
		if(this.creative)
			return this.getName();
		return new TranslationTextComponent("gui.lightmanscurrency.trading.title", this.getName(), this.ownerName);
	}
	
	protected class DataWriter implements Consumer<PacketBuffer>
	{
		
		UUID traderID;
		CompoundNBT traderCompound;
		
		public DataWriter(UUID traderID, CompoundNBT traderCompound)
		{
			this.traderID = traderID;
			this.traderCompound = traderCompound;
		}
		
		@Override
		public void accept(PacketBuffer buffer) {
			buffer.writeUniqueId(this.traderID);
			buffer.writeCompoundTag(this.traderCompound);
		}
	}
	
	protected class TradeIndexDataWriter implements Consumer<PacketBuffer>
	{
		UUID traderID;
		CompoundNBT traderCompound;
		int tradeIndex;
		
		public TradeIndexDataWriter(UUID traderID, CompoundNBT traderCompound, int tradeIndex)
		{
			this.traderID = traderID;
			this.traderCompound = traderCompound;
			this.tradeIndex = tradeIndex;
		}
		
		@Override
		public void accept(PacketBuffer buffer) {
			buffer.writeUniqueId(this.traderID);
			buffer.writeCompoundTag(this.traderCompound);
			buffer.writeInt(this.tradeIndex);
		}
	}
	
	public static boolean equals(UniversalTraderData data1, UniversalTraderData data2)
	{
		return data1.write(new CompoundNBT()).equals(data2.write(new CompoundNBT()));
	}
	
	public abstract ResourceLocation IconLocation();
	
	public abstract int IconPositionX();
	
	public abstract int IconPositionY();
	
}
