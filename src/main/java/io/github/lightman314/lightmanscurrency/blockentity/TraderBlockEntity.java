package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.MessageRequestNBT;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageRequestSyncUsers;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageSyncUsers;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public abstract class TraderBlockEntity extends TickableBlockEntity implements IOwnableBlockEntity{
	
	String customName = "";
	
	protected UUID ownerID = null;
	protected String ownerName = "";
	
	protected boolean isCreative = false;
	
	protected CoinValue storedMoney = new CoinValue();
	
	/** A list of players using this trader */
	private List<Player> users = new ArrayList<>();
	private int userCount = 0;
	
	private boolean versionUpdate = false;
	private int oldVersion = 0;
	
	private boolean firstTick = true;
	
	protected TraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}
	
	public void userOpen(Player player)
	{
		if(!users.contains(player))
		{
			//LightmansCurrency.LOGGER.info("Player with ID " + player.getUniqueID() + " has opened the trader.");
			users.add(player);
			sendUserUpdate();
		}
	}
	
	public void userClose(Player player)
	{
		if(users.contains(player))
		{
			//LightmansCurrency.LOGGER.info("Player with ID " + player.getUniqueID() + " has closed the trader.");
			users.remove(player);
			sendUserUpdate();
		}
	}
	
	public int getUserCount()
	{
		if(level.isClientSide)
			return this.userCount;
		else
			return this.users.size();
	}
	
	protected List<Player> getUsers() { return this.users; }
	
	private void sendUserUpdate()
	{
		if(!level.isClientSide)
		{
			LevelChunk chunk = (LevelChunk)this.level.getChunk(this.worldPosition);
			LightmansCurrencyPacketHandler.instance.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), new MessageSyncUsers(this.worldPosition, this.getUserCount()));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public void setUserCount(int value)
	{
		this.userCount = value;
	}
	
	/**
	 * Whether or not the given player is the owner of the trader.
	 * How this result is determined changed based on whether this is a creative trader or not.
	 */
	public boolean isOwner(Entity entity)
	{
		if(this.isCreative && entity instanceof Player)
		{
			Player player = (Player)entity;
			return player.hasPermissions(2) && player.isCreative();
		}
		else if(this.ownerID != null)
		{
			return entity.getUUID().equals(ownerID);
		}
		else
		{
			LightmansCurrency.LogError("Owner ID for the trading machine is null. Unable to determine if the owner is valid.");
			return true;
		}
	}
	
	/**
	 * Defines the owner of the Trader
	 * @param player The player that will be defined as the trader's owner.
	 */
	public void setOwner(Entity player)
	{
		//CurrencyMod.LOGGER.info("Defining the tile's owner. UUID: " + player.getUniqueID() + " Name: " + player.getName().getString());
		this.ownerID = player.getUUID();
		this.ownerName = player.getName().getString();
		
		if(!this.level.isClientSide)
		{
			CompoundTag compound = this.writeOwner(new CompoundTag());
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
			this.setChanged();
		}
	}
	
	/**
	 * Returns whether the player is allowed to break the block.
	 * @return Returns true if the player is the owner, or if the player is both creative & opped.
	 */
	public boolean canBreak(Player player)
	{
		if(isOwner(player))
			return true;
		return player.hasPermissions(2) && player.isCreative();
	}
	
	public boolean isCreative()
	{
		return this.isCreative;
	}
	
	public void toggleCreative()
	{
		this.isCreative = !this.isCreative;
		if(!this.level.isClientSide)
		{
			//Send update packet
			CompoundTag compound = this.writeCreative(new CompoundTag());
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
			this.setChanged();
		}
		
	}
	
	/**
	 * Gets the amount of stored money.
	 */
	public CoinValue getStoredMoney()
	{
		return storedMoney;
	}
	
	/**
	 * Adds the given amount of money to the stored money.
	 */
	public void addStoredMoney(CoinValue addedAmount)
	{
		storedMoney.addValue(addedAmount);
		if(!this.level.isClientSide)
		{
			CompoundTag compound = this.writeStoredMoney(new CompoundTag());
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
			this.setChanged();
		}
	}
	
	/**
	 * Removes the given amount of money to the stored money.
	 */
	public void removeStoredMoney(CoinValue removedAmount)
	{
		long newValue = this.storedMoney.getRawValue() - removedAmount.getRawValue();
		this.storedMoney.readFromOldValue(newValue);
		if(!this.level.isClientSide)
		{
			CompoundTag compound = this.writeStoredMoney(new CompoundTag());
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
			this.setChanged();
		}
	}
	
	/**
	 * Resets the stored money to 0.
	 */
	public void clearStoredMoney()
	{
		storedMoney = new CoinValue();
		if(!this.level.isClientSide)
		{
			CompoundTag compound = this.writeStoredMoney(new CompoundTag());
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
			this.setChanged();
		}
	}
	
	@Override
	public void serverTick()
	{
		if(this.versionUpdate && this.level != null)
		{
			this.versionUpdate = false;
			this.onVersionUpdate(oldVersion);
			this.setChanged();
		}
	}
	
	public Component getName()
	{
		if(this.customName != "")
			return new TextComponent(this.customName);
		if(this.level.isClientSide)
			return this.getBlockState().getBlock().getName();
		return new TextComponent("");
	}
	
	public Component getTitle()
	{
		if(this.isCreative)
			return this.getName();
		return new TranslatableComponent("gui.lightmanscurrency.trading.title", this.getName(), this.ownerName);
		
	}
	
	public boolean hasCustomName() { return this.customName != null; }
	
	public void setCustomName(String customName)
	{
		LightmansCurrency.LogInfo("Custom Name set to '" + customName + "'");
		this.customName = customName;
		if(!this.level.isClientSide)
		{
			CompoundTag compound = this.writeCustomName(new CompoundTag());
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
			this.setChanged();
		}
	}
	
	public abstract MenuProvider getTradeMenuProvider();
	
	public void openTradeMenu(Player player)
	{
		MenuProvider provider = getTradeMenuProvider();
		if(provider == null)
		{
			LightmansCurrency.LogError("No trade menu container provider was given for the trader of type " + this.getType().getRegistryName().toString());
			return;
		}
		if(!(player instanceof ServerPlayer))
		{
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the trade menu.");
			return;
		}
		NetworkHooks.openGui((ServerPlayer)player, provider, worldPosition);
	}
	
	public abstract MenuProvider getStorageMenuProvider(); 
	
	public void openStorageMenu(Player player)
	{
		MenuProvider provider = getStorageMenuProvider();
		if(provider == null)
		{
			LightmansCurrency.LogError("No storage container provider was given for the trader of type " + this.getType().getRegistryName().toString());
			return;
		}
		if(!(player instanceof ServerPlayer))
		{
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the storage menu.");
			return;
		}
		NetworkHooks.openGui((ServerPlayer)player, provider, worldPosition);
	}
	
	public abstract MenuProvider getCashRegisterTradeMenuProvider(CashRegisterBlockEntity cashRegister);
	
	public void openCashRegisterTradeMenu(Player player, CashRegisterBlockEntity cashRegister)
	{
		MenuProvider provider = getCashRegisterTradeMenuProvider(cashRegister);
		if(provider == null)
		{
			LightmansCurrency.LogError("No cash register container provider was given for the trader of type " + this.getType().getRegistryName().toString());
			return;
		}
		if(!(player instanceof ServerPlayer))
		{
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the cash register menu.");
			return;
		}
		NetworkHooks.openGui((ServerPlayer)player, provider, new CRDataWriter(worldPosition, cashRegister.getBlockPos()));
	}
	
	@Override
	public CompoundTag save(CompoundTag compound)
	{
		writeOwner(compound);
		writeStoredMoney(compound);
		writeCreative(compound);
		writeCustomName(compound);
		writeVersion(compound);
		
		return superWrite(compound);
	}
	
	public CompoundTag superWrite(CompoundTag compound)
	{
		return super.save(compound);
	}
	
	protected CompoundTag writeOwner(CompoundTag compound)
	{
		if(this.ownerID != null)
			compound.putUUID("OwnerID", this.ownerID);
		if(this.ownerName != "")
			compound.putString("OwnerName", this.ownerName);
		return compound;
	}
	
	protected CompoundTag writeStoredMoney(CompoundTag compound)
	{
		this.storedMoney.writeToNBT(compound, "StoredMoney");
		return compound;
	}
	
	protected CompoundTag writeCreative(CompoundTag compound)
	{
		compound.putBoolean("Creative", this.isCreative);
		return compound;
	}
	
	protected CompoundTag writeCustomName(CompoundTag compound)
	{
		compound.putString("CustomName", this.customName);
		return compound;
	}
	
	protected CompoundTag writeVersion(CompoundTag compound)
	{
		compound.putInt("TraderVersion", this.GetCurrentVersion());
		return compound;
	}
	
	@Override
	public void load(CompoundTag compound)
	{
		//Owner
		if(compound.contains("OwnerID"))
			this.ownerID = compound.getUUID("OwnerID");
		if(compound.contains("OwnerName", Constants.NBT.TAG_STRING))
			this.ownerName = compound.getString("OwnerName");
		//Stored Money
		//Load stored money
		if(compound.contains("StoredMoney", Constants.NBT.TAG_INT))
		{
			LightmansCurrency.LogInfo("Reading stored money from older value format. Will be updated to newer value format.");
			this.storedMoney.readFromOldValue(compound.getInt("StoredMoney"));
		}
		else if(compound.contains("StoredMoney"))
			this.storedMoney.readFromNBT(compound, "StoredMoney");
		//Creative
		if(compound.contains("Creative"))
			this.isCreative = compound.getBoolean("Creative");
		//Custom Name
		if(compound.contains("CustomName", Constants.NBT.TAG_STRING))
			this.customName = compound.getString("CustomName");
		
		//Version
		if(compound.contains("TraderVersion", Constants.NBT.TAG_INT))
			oldVersion = compound.getInt("TraderVersion");
		//Validate the version #
		if(oldVersion < this.GetCurrentVersion())
			this.versionUpdate = true; //Flag this to perform a version update later once the world has been defined
		
		super.load(compound);
	}
	
	protected abstract void onVersionUpdate(int oldVersion);
	
	public int GetCurrentVersion() { return 0; };
	
	@Override
	public void clientTick()
	{
		if(firstTick)
		{
			firstTick = false;
			onLoad();
		}
	}
	
	public void onLoad()
	{
		LightmansCurrency.LogInfo("Running onLoad code for block at " + this.getBlockPos().toString());
		super.onLoad();
		if(level.isClientSide)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRequestNBT(this));
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRequestSyncUsers(this.worldPosition));
		}
	}
	
	public void dumpContents(Level world, BlockPos pos)
	{
		List<ItemStack> coinItems = MoneyUtil.getCoinsOfValue(this.storedMoney);
		if(coinItems.size() > 0)
			InventoryUtil.dumpContents(world, pos, coinItems);
	}
	
	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.save(new CompoundTag()));
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
	{
		CompoundTag compound = pkt.getTag();
		//CurrencyMod.LOGGER.info("Loading NBT from update packet.");
		this.load(compound);
	}
	
	private class CRDataWriter implements Consumer<FriendlyByteBuf>
	{
		
		BlockPos traderPos;
		BlockPos registerPos;
		
		public CRDataWriter(BlockPos traderPos, BlockPos registerPos)
		{
			this.traderPos = traderPos;
			this.registerPos = registerPos;
		}
		
		@Override
		public void accept(FriendlyByteBuf buffer) {
			buffer.writeBlockPos(traderPos);
			buffer.writeBlockPos(registerPos);
			
		}
	}
	
	protected class TradeIndexDataWriter implements Consumer<FriendlyByteBuf>
	{
		BlockPos traderPos;
		int tradeIndex;
		
		public TradeIndexDataWriter(BlockPos traderPos, int tradeIndex)
		{
			this.traderPos = traderPos;
			this.tradeIndex = tradeIndex;
		}
		
		@Override
		public void accept(FriendlyByteBuf buffer)
		{
			buffer.writeBlockPos(traderPos);
			buffer.writeInt(tradeIndex);
		}
		
	}
	
}
