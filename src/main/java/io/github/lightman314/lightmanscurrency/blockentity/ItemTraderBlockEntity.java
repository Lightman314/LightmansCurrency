package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.item_trader.MessageSetTraderRules;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import io.github.lightman314.lightmanscurrency.menus.ItemEditMenu;
import io.github.lightman314.lightmanscurrency.menus.ItemTraderMenu;
import io.github.lightman314.lightmanscurrency.menus.ItemTraderMenuCR;
import io.github.lightman314.lightmanscurrency.menus.ItemTraderStorageMenu;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ILoggerSupport;
import io.github.lightman314.lightmanscurrency.api.ItemShopLogger;
import io.github.lightman314.lightmanscurrency.blocks.traderblocks.interfaces.IItemTraderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

public class ItemTraderBlockEntity extends TraderBlockEntity implements IItemTrader, ILoggerSupport<ItemShopLogger>, ITradeRuleHandler{
	
	public static final int TRADELIMIT = 16;
	public static final int VERSION = 1;
	
	protected Container storage;
	
	private final ItemShopLogger logger = new ItemShopLogger();
	
	protected int tradeCount = 1;
	
	private long rotationTime = 0;
	
	protected NonNullList<ItemTradeData> trades;
	
	List<TradeRule> tradeRules = new ArrayList<>();
	
	public ItemTraderBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModTileEntities.ITEM_TRADER, pos, state);
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.storage = new SimpleContainer(this.getSizeInventory());
	}
	
	public ItemTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount)
	{
		super(ModTileEntities.ITEM_TRADER, pos, state);
		this.tradeCount = tradeCount;
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.storage = new SimpleContainer(this.getSizeInventory());
	}
	
	protected ItemTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.storage = new SimpleContainer(this.getSizeInventory());
	}
	
	protected ItemTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int tradeCount)
	{
		super(type, pos, state);
		this.tradeCount = tradeCount;
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.storage = new SimpleContainer(this.getSizeInventory());
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
	
	public void addTrade()
	{
		if(this.level.isClientSide)
			return;
		if(tradeCount >= TRADELIMIT)
			return;
		overrideTradeCount(tradeCount + 1);
		forceReOpen();
	}
	
	public void removeTrade()
	{
		if(this.level.isClientSide)
			return;
		if(tradeCount <= 1)
			return;
		overrideTradeCount(tradeCount - 1);
		forceReOpen();
	}
	
	private void forceReOpen()
	{
		for(Player player : this.getUsers())
		{
			if(player.containerMenu instanceof ItemTraderStorageMenu)
				this.openStorageMenu(player);
			else if(player.containerMenu instanceof ItemTraderMenuCR)
				this.openCashRegisterTradeMenu(player, ((ItemTraderMenuCR)player.containerMenu).cashRegister);
			else if(player.containerMenu instanceof ItemTraderMenu)
				this.openTradeMenu(player);
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
		Container oldStorage = this.storage;
		this.storage = new SimpleContainer(this.getSizeInventory());
		for(int i = 0; i < this.storage.getContainerSize() && i < oldStorage.getContainerSize(); i++)
		{
			this.storage.setItem(i, oldStorage.getItem(i));
		}
		//Attempt to place lost items into the available slots
		if(oldStorage.getContainerSize() > this.getSizeInventory())
		{
			for(int i = this.getSizeInventory(); i < oldStorage.getContainerSize(); i++)
			{
				InventoryUtil.TryPutItemStack(this.storage, oldStorage.getItem(i));
			}
		}
		//Send an update to the client
		if(!this.level.isClientSide)
		{
			//Send update packet
			CompoundTag compound = this.writeTrades(new CompoundTag());
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
		if(!this.level.isClientSide)
		{
			//Send update packet
			CompoundTag compound = this.writeTrades(new CompoundTag());
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
		}
		this.setChanged();
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
		if(!this.level.isClientSide)
		{
			//Send update packet
			CompoundTag compound = this.writeLogger(new CompoundTag());
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
		}
		this.setChanged();
	}
	
	public int getTradeStock(int tradeSlot)
	{
		ItemTradeData trade = getTrade(tradeSlot);
		if(!trade.getSellItem().isEmpty())
		{
			if(this.isCreative)
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
	public CompoundTag save(CompoundTag compound)
	{
		
		this.writeItems(compound);
		this.writeTrades(compound);
		this.writeLogger(compound);
		this.writeTradeRules(compound);
		
		return super.save(compound);
		
	}
	
	protected CompoundTag writeItems(CompoundTag compound)
	{
		InventoryUtil.saveAllItems("Items", compound, this.storage);
		return compound;
	}
	
	public CompoundTag writeTrades(CompoundTag compound)
	{
		compound.putInt("TradeLimit", this.tradeCount);
		ItemTradeData.saveAllData(compound, this.trades);
		return compound;
	}
	
	public CompoundTag writeLogger(CompoundTag compound)
	{
		this.logger.write(compound);
		return compound;
	}
	
	public CompoundTag writeTradeRules(CompoundTag compound)
	{
		TradeRule.writeRules(compound, this.tradeRules);
		return compound;
	}
	
	@Override
	public void load(CompoundTag compound)
	{
		
		//Load the trade limit
		if(compound.contains("TradeLimit", Tag.TAG_INT))
			this.tradeCount = MathUtil.clamp(compound.getInt("TradeLimit"), 1, TRADELIMIT);
		//Load trades
		if(compound.contains(ItemTradeData.DEFAULT_KEY))
		{
			this.trades = ItemTradeData.loadAllData(compound, this.getTradeCount());
		}
		
		//Load the inventory
		if(compound.contains("Items"))
		{
			this.storage = InventoryUtil.loadAllItems("Items", compound, this.getSizeInventory());
		}
		
		//Load the shop logger
		this.logger.read(compound);
		
		//Load the trade rules
		if(compound.contains(TradeRule.DEFAULT_TAG, Tag.TAG_LIST))
			this.tradeRules = TradeRule.readRules(compound);
		
		super.load(compound);
		
	}
	
	@Override
	public void dumpContents(Level world, BlockPos pos)
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
	public AABB getRenderBoundingBox()
	{
		return new AABB(this.worldPosition.offset(-1, 0, -1), this.worldPosition.offset(2,2,2));
	}

	@Override
	public void tick() {
		super.tick();
		if(this.level.isClientSide)
			this.rotationTime++;
	}
	
	@Override
	public MenuProvider getTradeMenuProvider() { return new TradeContainerProvider(this); }

	@Override
	public MenuProvider getStorageMenuProvider() { return new StorageContainerProvider(this); }

	@Override
	public MenuProvider getCashRegisterTradeMenuProvider(CashRegisterBlockEntity cashRegister) { return new TradeCRContainerProvider(this, cashRegister); }
	
	protected MenuProvider getItemEditMenuProvider(int tradeIndex) { return new ItemEditContainerProvider(this, tradeIndex); }
	
	public void openItemEditMenu(Player player, int tradeIndex)
	{
		MenuProvider provider = getItemEditMenuProvider(tradeIndex);
		if(provider == null)
		{
			LightmansCurrency.LogError("No item edit container provider was given for the trader of type " + this.getType().getRegistryName().toString());
			return;
		}
		if(!(player instanceof ServerPlayer))
		{
			LightmansCurrency.LogError("Player is not a server player entity. Cannot open the storage menu.");
			return;
		}
		NetworkHooks.openGui((ServerPlayer)player, provider, new TradeIndexDataWriter(this.worldPosition, tradeIndex));
	}
	
	private class TradeContainerProvider implements MenuProvider{

		ItemTraderBlockEntity tileEntity;
		
		public TradeContainerProvider(ItemTraderBlockEntity tileEntity)
		{
			this.tileEntity = tileEntity;
		}
		
		public Component getDisplayName()
		{
			return tileEntity.getName();
		}
		
		public AbstractContainerMenu createMenu(int id, Inventory inventory, Player entity)
		{
			return new ItemTraderMenu(id, inventory, tileEntity);
		}
		
	}
	
	private class StorageContainerProvider implements MenuProvider{

		ItemTraderBlockEntity tileEntity;
		
		public StorageContainerProvider(ItemTraderBlockEntity tileEntity)
		{
			this.tileEntity = tileEntity;
		}
		
		public Component getDisplayName()
		{
			return tileEntity.getName();
		}
		
		public AbstractContainerMenu createMenu(int id, Inventory inventory, Player entity)
		{
			return new ItemTraderStorageMenu(id, inventory, tileEntity);
		}
		
	}
	
	private class TradeCRContainerProvider implements MenuProvider{

		ItemTraderBlockEntity tileEntity;
		CashRegisterBlockEntity registerEntity;
		
		public TradeCRContainerProvider(ItemTraderBlockEntity tileEntity, CashRegisterBlockEntity registerEntity)
		{
			this.tileEntity = tileEntity;
			this.registerEntity = registerEntity;
		}
		
		public Component getDisplayName()
		{
			return tileEntity.getName();
		}
		
		public AbstractContainerMenu createMenu(int id, Inventory inventory, Player entity)
		{
			return new ItemTraderMenuCR(id, inventory, tileEntity, registerEntity);
		}
		
	}
	
	private class ItemEditContainerProvider implements MenuProvider{

		ItemTraderBlockEntity tileEntity;
		int tradeIndex;
		
		public ItemEditContainerProvider(ItemTraderBlockEntity tileEntity, int tradeIndex)
		{
			this.tileEntity = tileEntity;
			this.tradeIndex = tradeIndex;
		}
		
		public Component getDisplayName()
		{
			return tileEntity.getName();
		}
		
		public AbstractContainerMenu createMenu(int id, Inventory inventory, Player entity)
		{
			return new ItemEditMenu(id, inventory, () -> tileEntity, tradeIndex);
		}
		
	}

	@Override
	public Container getStorage() {
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
					InventoryUtil.dumpContents(this.level, this.worldPosition, InventoryUtil.buildInventory(tradeStack));
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
		if(!this.level.isClientSide)
		{
			//Send update packet
			CompoundTag compound = this.writeTradeRules(new CompoundTag());
			TileEntityUtil.sendUpdatePacket(this, superWrite(compound));
		}
		this.setChanged();
	}
	
	public void closeRuleScreen(Player player)
	{
		this.openStorageMenu(player);
	}
	
	public ITradeRuleScreenHandler GetRuleScreenBackHandler() { return new TraderScreenHandler(this); }
	
	private static class TraderScreenHandler implements ITradeRuleScreenHandler
	{
		
		private final ItemTraderBlockEntity tileEntity;
		
		public TraderScreenHandler(ItemTraderBlockEntity tileEntity)
		{
			this.tileEntity = tileEntity;
		}
		
		@Override
		public ITradeRuleHandler ruleHandler() { return this.tileEntity; }
		
		@Override
		public void reopenLastScreen()
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenStorage(this.tileEntity.worldPosition));
		}
		
		@Override
		public void updateServer(List<TradeRule> newRules)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTraderRules(this.tileEntity.worldPosition, newRules));
		}
		
	}
	
}
