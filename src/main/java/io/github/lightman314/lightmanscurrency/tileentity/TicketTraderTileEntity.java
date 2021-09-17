package io.github.lightman314.lightmanscurrency.tileentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.blocks.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.client.gui.screen.ITradeRuleScreenHandler;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces.ITradeButtonStockSource;
import io.github.lightman314.lightmanscurrency.containers.TicketTraderContainer;
import io.github.lightman314.lightmanscurrency.containers.TicketTraderContainerCR;
import io.github.lightman314.lightmanscurrency.containers.TicketTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.ticket_trader.MessageSetTraderRules3;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenStorage;
import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.tradedata.TicketTradeData;
import io.github.lightman314.lightmanscurrency.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.ItemStackHelper;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class TicketTraderTileEntity extends TraderTileEntity implements ITradeButtonStockSource, ILoggerSupport<ItemShopLogger>, ITradeRuleHandler{
	
	public static final int TRADELIMIT = 16;
	public static final int VERSION = 0;
	
	protected NonNullList<ItemStack> inventory;
	
	private final ItemShopLogger logger = new ItemShopLogger();
	
	protected NonNullList<TicketTradeData> trades;
	protected int tradeCount = 1;
	
	protected IInventory storage;
	
	List<TicketTraderStorageContainer> storageContainers = new ArrayList<>();
	
	List<TradeRule> tradeRules = new ArrayList<>();
	
	public TicketTraderTileEntity()
	{
		super(ModTileEntities.TICKET_TRADER);
		this.storage = new Inventory(this.getSizeInventory());
		this.trades = TicketTradeData.listOfSize(tradeCount);
		this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
	}
	
	public TicketTraderTileEntity(int tradeCount)
	{
		super(ModTileEntities.TICKET_TRADER);
		this.tradeCount = tradeCount;
		this.storage = new Inventory(this.getSizeInventory());
		this.trades = TicketTradeData.listOfSize(tradeCount);
		this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
	}
	
	protected TicketTraderTileEntity(TileEntityType<?> type)
	{
		super(type);
		this.storage = new Inventory(this.getSizeInventory());
		this.trades = TicketTradeData.listOfSize(tradeCount);
		this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
	}
	
	protected TicketTraderTileEntity(TileEntityType<?> type, int tradeCount)
	{
		super(type);
		this.tradeCount = tradeCount;
		this.storage = new Inventory(this.getSizeInventory());
		this.trades = TicketTradeData.listOfSize(tradeCount);
		this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
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
		if(this.world.isRemote)
			return;
		if(tradeCount >= TRADELIMIT)
			return;
		overrideTradeCount(tradeCount + 1);
		forceReOpen();
	}
	
	public void removeTrade()
	{
		if(this.world.isRemote)
			return;
		if(tradeCount <= 1)
			return;
		overrideTradeCount(tradeCount - 1);
		forceReOpen();
	}
	
	private void forceReOpen()
	{
		for(PlayerEntity player : this.getUsers())
		{
			ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
			if(player.openContainer instanceof TicketTraderStorageContainer)
				this.openStorageMenu(serverPlayer);
			else if(player.openContainer instanceof TicketTraderContainerCR)
				this.openCashRegisterTradeMenu(serverPlayer, ((TicketTraderContainerCR)player.openContainer).cashRegister);
			else if(player.openContainer instanceof TicketTraderContainer)
				this.openTradeMenu(serverPlayer);
		}
	}
	
	public void overrideTradeCount(int newTradeCount)
	{
		if(tradeCount == newTradeCount)
			return;
		this.tradeCount = MathUtil.clamp(newTradeCount, 1, TRADELIMIT);
		NonNullList<TicketTradeData> oldTrades = this.trades;
		trades = TicketTradeData.listOfSize(getTradeCount());
		//Write the old trade data into the array.
		for(int i = 0; i < oldTrades.size() && i < trades.size(); i++)
		{
			trades.set(i, oldTrades.get(i));
		}
		//Set the new inventory list
		NonNullList<ItemStack> oldInventory = this.inventory;
		this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		for(int i = 0; i < this.inventory.size() && i < oldInventory.size(); i++)
		{
			this.inventory.set(i, oldInventory.get(i));
		}
		//Attempt to place lost items into the available slots
		if(oldInventory.size() > this.getSizeInventory())
		{
			for(int i = this.getSizeInventory(); i < oldInventory.size(); i++)
			{
				InventoryUtil.TryPutItemStack(this.getStorage(), oldInventory.get(i));
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
	
	public TicketTradeData getTrade(int tradeSlot)
	{
		if(tradeSlot < 0 || tradeSlot >= this.trades.size())
		{
			LightmansCurrency.LogError("Cannot get trade in index " + tradeSlot + " from a trader with only " + this.trades.size() + " trades.");
			return new TicketTradeData();
		}
		return this.trades.get(tradeSlot);
	}
	
	public NonNullList<TicketTradeData> getAllTrades()
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
		TicketTradeData trade = getTrade(tradeSlot);
		if(trade.getTicketID() != null)
		{
			if(this.isCreative)
				return Integer.MAX_VALUE;
			else
			{
				return trade.stockCount(this.storage);
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
	
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		
		this.writeItems(compound);
		this.writeTrades(compound);
		this.writeLogger(compound);
		this.writeTradeRules(compound);
		
		return super.write(compound);
		
	}
	
	protected CompoundNBT writeItems(CompoundNBT compound)
	{
		ItemStackHelper.saveAllItems("Items", compound, this.inventory);
		return compound;
	}
	
	public CompoundNBT writeTrades(CompoundNBT compound)
	{
		compound.putInt("TradeLimit", this.tradeCount);
		TicketTradeData.saveAllData(compound, this.trades);
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
			this.trades = TicketTradeData.loadAllData(compound, this.getTradeCount());
			SyncContainerListeners();
		}
		
		//Load the inventory
		if(this.inventory == null)
			this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if(compound.contains("Items"))
		{
			this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
			ItemStackHelper.loadAllItems("Items", compound, this.inventory);
		}
		
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
		InventoryUtil.dumpContents(world, pos, this.inventory);
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
	}
	
	public void AddContainerListener(TicketTraderStorageContainer container)
	{
		if(!storageContainers.contains(container))
			storageContainers.add(container);
	}
	
	public void RemoveContainerListener(TicketTraderStorageContainer container)
	{
		if(storageContainers.contains(container))
			storageContainers.remove(container);
	}
	
	private void SyncContainerListeners()
	{
		storageContainers.forEach(container ->
		{
			container.resyncTrades();
		});
	}
	
	@Override
	public INamedContainerProvider getTradeMenuProvider() { return new TradeContainerProvider(this); }

	@Override
	public INamedContainerProvider getStorageMenuProvider() { return new StorageContainerProvider(this); }

	@Override
	public INamedContainerProvider getCashRegisterTradeMenuProvider(CashRegisterTileEntity cashRegister) { return new TradeCRContainerProvider(this, cashRegister); }
	
	private class TradeContainerProvider implements INamedContainerProvider{

		TicketTraderTileEntity tileEntity;
		
		public TradeContainerProvider(TicketTraderTileEntity tileEntity)
		{
			this.tileEntity = tileEntity;
		}
		
		public ITextComponent getDisplayName()
		{
			return tileEntity.getName();
		}
		
		public Container createMenu(int id, PlayerInventory inventory, PlayerEntity entity)
		{
			return new TicketTraderContainer(id, inventory, tileEntity);
		}
		
	}
	
	private class StorageContainerProvider implements INamedContainerProvider{

		TicketTraderTileEntity tileEntity;
		
		public StorageContainerProvider(TicketTraderTileEntity tileEntity)
		{
			this.tileEntity = tileEntity;
		}
		
		public ITextComponent getDisplayName()
		{
			return tileEntity.getName();
		}
		
		public Container createMenu(int id, PlayerInventory inventory, PlayerEntity entity)
		{
			return new TicketTraderStorageContainer(id, inventory, tileEntity);
		}
		
	}
	
	private class TradeCRContainerProvider implements INamedContainerProvider{

		TicketTraderTileEntity tileEntity;
		CashRegisterTileEntity registerEntity;
		
		public TradeCRContainerProvider(TicketTraderTileEntity tileEntity, CashRegisterTileEntity registerEntity)
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
			return new TicketTraderContainerCR(id, inventory, tileEntity, registerEntity);
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
		
	}

	@Override
	public void beforeTrade(PreTradeEvent event) {
		this.tradeRules.forEach(rule -> rule.beforeTrade(event));
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
		
		private final TicketTraderTileEntity tileEntity;
		
		public TraderScreenHandler(TicketTraderTileEntity tileEntity)
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
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTraderRules3(this.tileEntity.pos, newRules));
		}
		
	}
	
}
