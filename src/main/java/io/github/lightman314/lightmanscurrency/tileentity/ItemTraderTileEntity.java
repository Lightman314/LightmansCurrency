package io.github.lightman314.lightmanscurrency.tileentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.blocks.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainer;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainerCR;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.MessageSetTraderRules;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.tileentity.ItemInterfaceTileEntity.IItemHandlerBlock;
import io.github.lightman314.lightmanscurrency.tileentity.ItemInterfaceTileEntity.IItemHandlerTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.handler.TraderItemHandler;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.ItemTraderSettings;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.settings.ItemTraderSettings.ItemHandlerSettings;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.api.ItemShopLogger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemTraderTileEntity extends TraderTileEntity implements IItemTrader, ILoggerSupport<ItemShopLogger>, ITradeRuleHandler {
	
	public static final int TRADELIMIT = 16;
	public static final int VERSION = 1;
	
	TraderItemHandler itemHandler = new TraderItemHandler(this);
	
	public IItemHandler getItemHandler(Direction relativeSide)
	{
		ItemHandlerSettings handlerSettings = this.itemSettings.getHandlerSetting(relativeSide);
		return this.itemHandler.getHandler(handlerSettings);
	}
	
	ItemTraderSettings itemSettings = new ItemTraderSettings(this, this::markItemSettingsDirty, this::sendSettingsUpdateToServer);
	
	@Override
	public Map<String,Integer> getAllyDefaultPermissions() { return ImmutableMap.of(Permissions.ItemTrader.EXTERNAL_INPUTS, 1); }
	
	protected IInventory storage = new Inventory(this.getSizeInventory());
	
	private final ItemShopLogger logger = new ItemShopLogger();
	
	protected int tradeCount = 1;
	
	private long rotationTime = 0;
	
	protected NonNullList<ItemTradeData> trades;
	
	List<TradeRule> tradeRules = new ArrayList<>();
	
	public ItemTraderTileEntity()
	{
		super(ModTileEntities.ITEM_TRADER);
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.storage = new Inventory(this.getSizeInventory());
	}
	
	public ItemTraderTileEntity(int tradeCount)
	{
		super(ModTileEntities.ITEM_TRADER);
		this.tradeCount = tradeCount;
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.storage = new Inventory(this.getSizeInventory());
	}
	
	protected ItemTraderTileEntity(TileEntityType<?> type)
	{
		super(type);
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.storage = new Inventory(this.getSizeInventory());
	}
	
	protected ItemTraderTileEntity(TileEntityType<?> type, int tradeCount)
	{
		super(type);
		this.tradeCount = tradeCount;
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.storage = new Inventory(this.getSizeInventory());
	}
	
	public void restrictTrade(int index, ItemTradeRestriction restriction)
	{
		getTrade(index).setRestriction(restriction);
	}
	
	public int getSizeInventory()
	{
		return getTradeCount() * 9;
	}
	
	public int getTradeCount()
	{
		//Limit trade count to 16 due to screen size limitations
		return MathUtil.clamp(tradeCount, 1, TRADELIMIT);
	}
	
	public int getTradeCountLimit()
	{
		return TRADELIMIT;
	}
	
	public void requestAddOrRemoveTrade(boolean isAdd)
	{
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(this.pos, isAdd));
	}
	
	public void addTrade(PlayerEntity requestor)
	{
		if(this.world.isRemote)
			return;
		if(tradeCount >= TRADELIMIT)
			return;
		if(TradingOffice.isAdminPlayer(requestor))
		{
			overrideTradeCount(tradeCount + 1);
			this.coreSettings.getLogger().LogAddRemoveTrade(requestor, true, this.tradeCount);
			this.markCoreSettingsDirty();
			forceReOpen();
		}
		else
			Settings.PermissionWarning(requestor, "add a trade slot", Permissions.ADMIN_MODE);
	}
	
	public void removeTrade(PlayerEntity requestor)
	{
		if(this.world.isRemote)
			return;
		if(tradeCount <= 1)
			return;
		if(TradingOffice.isAdminPlayer(requestor))
		{
			overrideTradeCount(tradeCount - 1);
			this.coreSettings.getLogger().LogAddRemoveTrade(requestor, false, this.tradeCount);
			this.markCoreSettingsDirty();
			forceReOpen();
		}
		else
			Settings.PermissionWarning(requestor, "remove a trade slot", Permissions.ADMIN_MODE);
		
	}
	
	private void forceReOpen()
	{
		for(PlayerEntity player : this.getUsers())
		{
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
			if(player.openContainer instanceof ItemTraderStorageContainer)
				this.openStorageMenu(serverPlayer);
			else if(player.openContainer instanceof ItemTraderContainerCR)
				this.openCashRegisterTradeMenu(serverPlayer, ((ItemTraderContainerCR)player.openContainer).cashRegister);
			else if(player.openContainer instanceof ItemTraderContainer)
				this.openTradeMenu(serverPlayer);
		}
	}
	
	public void overrideTradeCount(int newTradeCount)
	{
		if(tradeCount == newTradeCount)
			return;
		this.tradeCount = MathUtil.clamp(newTradeCount, 1, TRADELIMIT);
		NonNullList<ItemTradeData> oldTrades = trades;
		trades = ItemTradeData.listOfSize(getTradeCount());
		//Write the old trade data into the array.
		for(int i = 0; i < oldTrades.size() && i < trades.size(); i++)
		{
			trades.set(i, oldTrades.get(i));
		}
		//Set the new inventory list
		IInventory oldStorage = this.storage;
		this.storage = new Inventory(this.getSizeInventory());
		for(int i = 0; i < this.storage.getSizeInventory() && i < oldStorage.getSizeInventory(); i++)
		{
			this.storage.setInventorySlotContents(i, oldStorage.getStackInSlot(i));
		}
		//Attempt to place lost items into the available slots
		if(oldStorage.getSizeInventory() > this.storage.getSizeInventory())
		{
			for(int i = this.storage.getSizeInventory(); i < oldStorage.getSizeInventory(); i++)
			{
				InventoryUtil.TryPutItemStack(this.storage, oldStorage.getStackInSlot(i));
			}
		}
		//Send an update to the client
		if(!this.world.isRemote)
		{
			//Send update packet
			CompoundNBT compound = this.writeTrades(new CompoundNBT());
			this.writeItems(compound);
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
		}
		
	}
	
	public ItemTradeData getTrade(int tradeSlot)
	{
		if(tradeSlot < 0 || tradeSlot >= this.trades.size())
		{
			LightmansCurrency.LogError("Cannot get trade in index " + tradeSlot + " from a trader with only " + this.trades.size() + " trades.");
			return new ItemTradeData();
		}
		return this.trades.get(tradeSlot);
	}
	
	public NonNullList<ItemTradeData> getAllTrades()
	{
		return this.trades;
	}
	
	public void markTradesDirty()
	{
		//Send an update to the client
		if(!this.world.isRemote)
		{
			//Send update packet
			CompoundNBT compound = this.writeTrades(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
		}
		this.markDirty();
	}
	
	public ItemTraderSettings getItemSettings()
	{
		return this.itemSettings;
	}
	
	public List<Settings> getAdditionalSettings() { return Lists.newArrayList(this.itemSettings); }
	
	public void markItemSettingsDirty()
	{
		if(!this.world.isRemote)
		{
			CompoundNBT compound = this.writeItemSettings(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
		}
		this.markDirty();
	}
	
	public ItemShopLogger getLogger() {return this.logger; }
	
	public void clearLogger()
	{
		this.logger.clear();
		markLoggerDirty();
	}
	
	public void markLoggerDirty()
	{
		//Send an update to the client
		if(!this.world.isRemote)
		{
			//Send update packet
			CompoundNBT compound = this.writeLogger(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
		}
		this.markDirty();
	}
	
	public int getTradeStock(int tradeSlot)
	{
		ItemTradeData trade = getTrade(tradeSlot);
		if(!trade.getSellItem().isEmpty())
		{
			if(this.coreSettings.isCreative())
				return Integer.MAX_VALUE;
			else
			{
				return (int)trade.stockCount(this);
			}
		}
		return 0;
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<Vector3f> GetStackRenderPos(int tradeSlot, boolean isBlock)
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IItemTraderBlock)
		{
			IItemTraderBlock traderBlock = (IItemTraderBlock)block;
			return traderBlock.GetStackRenderPos(tradeSlot, this.getBlockState(), isBlock);
		}
		else
		{
			List<Vector3f> posList = new ArrayList<>();
			posList.add(new Vector3f(0.0f, 0.0f, 0.0f));
			return posList;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<Quaternion> GetStackRenderRot(int tradeSlot, float partialTicks, boolean isBlock)
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IItemTraderBlock)
		{
			IItemTraderBlock traderBlock = (IItemTraderBlock)block;
			List<Quaternion> rotation = traderBlock.GetStackRenderRot(tradeSlot, this.getBlockState(), isBlock);
			//If null received. Rotate item based on world time
			if(rotation == null)
			{
				rotation = new ArrayList<>();
				rotation.add(Vector3f.YP.rotationDegrees((this.rotationTime + partialTicks) * 2.0F));
			}
			return rotation;
		}
		else
		{
			List<Quaternion> rotation = new ArrayList<>();
			rotation.add(Vector3f.YP.rotationDegrees(0f));
			return rotation;
		}
			
	}
	
	@OnlyIn(Dist.CLIENT)
	public Vector3f GetStackRenderScale(int tradeSlot, boolean isBlock)
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IItemTraderBlock)
		{
			IItemTraderBlock traderBlock = (IItemTraderBlock)block;
			return traderBlock.GetStackRenderScale(tradeSlot, this.getBlockState(), isBlock);
		}
		else
			return new Vector3f(0.0f, 0.0f, 0.0f);
	}
	
	@OnlyIn(Dist.CLIENT)
	public int maxRenderIndex()
	{
		Block block = this.getBlockState().getBlock();
		if(block instanceof IItemTraderBlock)
		{
			IItemTraderBlock traderBlock = (IItemTraderBlock)block;
			return traderBlock.maxRenderIndex();
		}
		else
			return 0;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		
		this.writeItems(compound);
		this.writeTrades(compound);
		this.writeItemSettings(compound);
		this.writeLogger(compound);
		this.writeTradeRules(compound);
		
		return super.write(compound);
		
	}
	
	protected CompoundNBT writeItems(CompoundNBT compound)
	{
		InventoryUtil.saveAllItems("Items", compound, this.storage);
		return compound;
	}
	
	public CompoundNBT writeTrades(CompoundNBT compound)
	{
		compound.putInt("TradeLimit", this.tradeCount);
		ItemTradeData.saveAllData(compound, this.trades);
		return compound;
	}
	
	public CompoundNBT writeItemSettings(CompoundNBT compound)
	{
		compound.put("ItemSettings", this.itemSettings.save(new CompoundNBT()));
		return compound;
	}
	
	public CompoundNBT writeLogger(CompoundNBT compound)
	{
		this.logger.write(compound);
		return compound;
	}
	
	public CompoundNBT writeTradeRules(CompoundNBT compound)
	{
		TradeRule.writeRules(compound, this.tradeRules);
		return compound;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT compound)
	{
		
		//Load the trade limit
		if(compound.contains("TradeLimit", Constants.NBT.TAG_INT))
			this.tradeCount = MathUtil.clamp(compound.getInt("TradeLimit"), 1, TRADELIMIT);
		//Load trades
		if(compound.contains(ItemTradeData.DEFAULT_KEY))
		{
			this.trades = ItemTradeData.loadAllData(compound, this.getTradeCount());
		}
		
		//Load the inventory
		if(this.storage == null)
			this.storage = new Inventory(this.getSizeInventory());
		if(compound.contains("Items"))
		{
			this.storage = InventoryUtil.loadAllItems("Items", compound, this.getSizeInventory());
		}
		
		//Load the settings
		if(compound.contains("ItemSettings", Constants.NBT.TAG_COMPOUND))
			this.itemSettings.load(compound.getCompound("ItemSettings"));
		
		//Load the shop logger
		this.logger.read(compound);
		
		//Load the trade rules
		if(compound.contains(TradeRule.DEFAULT_TAG, Constants.NBT.TAG_LIST))
			this.tradeRules = TradeRule.readRules(compound);
		
		super.read(state, compound);
		
	}
	
	@Override
	public void dumpContents(World world, BlockPos pos)
	{
		//super.dumpContents dumps the coins automatically
		super.dumpContents(world, pos);
		//Dump the Inventory
		InventoryUtil.dumpContents(world, pos, this.storage);
		//Dump the Trade Inventory
		//Removed as the trade inventory no longer consumes items
		//InventoryUtil.dumpContents(world, pos, new TradeInventory(trades));
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(2,2,2));
	}

	@Override
	public void tick() {
		super.tick();
		if(this.world.isRemote)
			this.rotationTime++;
	}
	
	@Override
	public INamedContainerProvider getTradeMenuProvider() { return new TradeContainerProvider(this); }

	@Override
	public INamedContainerProvider getStorageMenuProvider() { return new StorageContainerProvider(this); }

	@Override
	public INamedContainerProvider getCashRegisterTradeMenuProvider(CashRegisterTileEntity cashRegister) { return new TradeCRContainerProvider(this, cashRegister); }
	
	protected INamedContainerProvider getItemEditMenuProvider(int tradeIndex) { return new ItemEditContainerProvider(this, tradeIndex); }
	
	public void openItemEditMenu(PlayerEntity player, int tradeIndex)
	{
		INamedContainerProvider provider = getItemEditMenuProvider(tradeIndex);
		if(provider == null)
		{
			LightmansCurrency.LogError("No item edit container provider was given for the trader of type " + this.getType().getRegistryName().toString());
			return;
		}
		if(!(player instanceof ServerPlayerEntity))
		{
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the storage menu.");
			return;
		}
		NetworkHooks.openGui((ServerPlayerEntity)player, provider, new TradeIndexDataWriter(this.pos, tradeIndex));
	}
	
	private class TradeContainerProvider implements INamedContainerProvider{

		ItemTraderTileEntity tileEntity;
		
		public TradeContainerProvider(ItemTraderTileEntity tileEntity)
		{
			this.tileEntity = tileEntity;
		}
		
		public ITextComponent getDisplayName()
		{
			return tileEntity.getName();
		}
		
		public Container createMenu(int id, PlayerInventory inventory, PlayerEntity entity)
		{
			return new ItemTraderContainer(id, inventory, tileEntity);
		}
		
	}
	
	private class StorageContainerProvider implements INamedContainerProvider{

		ItemTraderTileEntity tileEntity;
		
		public StorageContainerProvider(ItemTraderTileEntity tileEntity)
		{
			this.tileEntity = tileEntity;
		}
		
		public ITextComponent getDisplayName()
		{
			return tileEntity.getName();
		}
		
		public Container createMenu(int id, PlayerInventory inventory, PlayerEntity entity)
		{
			return new ItemTraderStorageContainer(id, inventory, tileEntity);
		}
		
	}
	
	private class TradeCRContainerProvider implements INamedContainerProvider{

		ItemTraderTileEntity tileEntity;
		CashRegisterTileEntity registerEntity;
		
		public TradeCRContainerProvider(ItemTraderTileEntity tileEntity, CashRegisterTileEntity registerEntity)
		{
			this.tileEntity = tileEntity;
			this.registerEntity = registerEntity;
		}
		
		public ITextComponent getDisplayName()
		{
			return tileEntity.getName();
		}
		
		public Container createMenu(int id, PlayerInventory inventory, PlayerEntity entity)
		{
			return new ItemTraderContainerCR(id, inventory, tileEntity, registerEntity);
		}
		
	}
	
	private class ItemEditContainerProvider implements INamedContainerProvider{

		ItemTraderTileEntity tileEntity;
		int tradeIndex;
		
		public ItemEditContainerProvider(ItemTraderTileEntity tileEntity, int tradeIndex)
		{
			this.tileEntity = tileEntity;
			this.tradeIndex = tradeIndex;
		}
		
		public ITextComponent getDisplayName()
		{
			return tileEntity.getName();
		}
		
		public Container createMenu(int id, PlayerInventory inventory, PlayerEntity entity)
		{
			return new ItemEditContainer(id, inventory, () -> tileEntity, tradeIndex);
		}
		
	}

	@Override
	public IInventory getStorage() {
		return this.storage;
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
					tradeStack = InventoryUtil.TryPutItemStack(this.storage, tradeStack);
				if(!tradeStack.isEmpty())
					InventoryUtil.dumpContents(this.world, pos, InventoryUtil.buildInventory(tradeStack));
			}
		}
		
	}

	@Override
	public void beforeTrade(PreTradeEvent event) {
		this.tradeRules.forEach(rule -> rule.beforeTrade(event));
	}
	
	@Override
	public void tradeCost(TradeCostEvent event)
	{
		this.tradeRules.forEach(rule -> rule.tradeCost(event));
	}

	@Override
	public void afterTrade(PostTradeEvent event) {
		this.tradeRules.forEach(rule -> rule.afterTrade(event));
	}
	
	public void addRule(TradeRule newRule)
	{
		if(newRule == null)
			return;
		//Confirm a lack of duplicate rules
		for(int i = 0; i < this.tradeRules.size(); i++)
		{
			if(newRule.type == this.tradeRules.get(i).type)
			{
				LightmansCurrency.LogInfo("Blocked rule addition due to rule of same type already present.");
				return;
			}
		}
		this.tradeRules.add(newRule);
	}
	
	public List<TradeRule> getRules() { return this.tradeRules; }
	
	public void setRules(List<TradeRule> rules) { this.tradeRules = rules; }
	
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
		//Send an update to the client
		if(!this.world.isRemote)
		{
			//Send update packet
			CompoundNBT compound = this.writeTradeRules(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
		}
		this.markDirty();
	}
	
	public void closeRuleScreen(PlayerEntity player)
	{
		this.openStorageMenu(player);
	}
	
	public ITradeRuleScreenHandler GetRuleScreenBackHandler() { return new TraderScreenHandler(this); }
	
	private static class TraderScreenHandler implements ITradeRuleScreenHandler
	{
		
		private final ItemTraderTileEntity tileEntity;
		
		public TraderScreenHandler(ItemTraderTileEntity tileEntity)
		{
			this.tileEntity = tileEntity;
		}
		
		@Override
		public ITradeRuleHandler ruleHandler() { return this.tileEntity; }
		
		@Override
		public void reopenLastScreen()
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.tileEntity.pos));
		}
		
		@Override
		public void updateServer(List<TradeRule> newRules)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTraderRules(this.tileEntity.pos, newRules));
		}
		
	}
	
	//Item capability for hopper and item automation
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			Block block = this.getBlockState().getBlock();
			if(block instanceof IItemHandlerBlock)
			{
				IItemHandlerBlock handlerBlock = (IItemHandlerBlock)block;
				IItemHandlerTileEntity tileEntity = handlerBlock.getItemHandlerEntity(this.getBlockState(), this.world, this.pos);
				if(tileEntity != null)
				{
					IItemHandler handler = tileEntity.getItemHandler(handlerBlock.getRelativeSide(this.getBlockState(), side));
					if(handler != null)
						return LazyOptional.of(() -> handler).cast();
					else
						return LazyOptional.empty();
				}
			}
		}
		return super.getCapability(cap, side);
	}
	
}
