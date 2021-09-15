package io.github.lightman314.lightmanscurrency.tileentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.blocks.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces.ITradeButtonStockSource;
import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainer;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainerCR;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.interfaces.IItemTrader;
import io.github.lightman314.lightmanscurrency.core.ModTileEntities;
import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData;
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
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemTraderTileEntity extends TraderTileEntity implements IInventory, ITradeButtonStockSource, IItemTrader, ILoggerSupport<ItemShopLogger>{
	
	public static final int TRADELIMIT = 16;
	public static final int VERSION = 1;
	
	protected NonNullList<ItemStack> inventory;
	
	private final ItemShopLogger logger = new ItemShopLogger();
	
	protected int tradeCount = 1;
	
	private long rotationTime = 0;
	
	protected NonNullList<ItemTradeData> trades;
	
	List<ItemTraderStorageContainer> storageContainers = new ArrayList<>();
	
	public ItemTraderTileEntity()
	{
		super(ModTileEntities.ITEM_TRADER);
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
	}
	
	public ItemTraderTileEntity(int tradeCount)
	{
		super(ModTileEntities.ITEM_TRADER);
		this.tradeCount = tradeCount;
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
	}
	
	protected ItemTraderTileEntity(TileEntityType<?> type)
	{
		super(type);
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
	}
	
	protected ItemTraderTileEntity(TileEntityType<?> type, int tradeCount)
	{
		super(type);
		this.tradeCount = tradeCount;
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
	}
	
	public void restrictTrade(int index, ItemTradeData.ItemTradeRestrictions restriction)
	{
		getTrade(index).setRestriction(restriction);
	}
	
	@Override
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
				InventoryUtil.TryPutItemStack(this, oldInventory.get(i));
			}
		}
		//Send an update to the client
		if(!this.world.isRemote)
		{
			//Send update packet
			CompoundNBT compound = this.writeTrades(new CompoundNBT());
			this.writeItems(compound);
			TileEntityUtil.sendUpdatePacket(this, super.write(compound));
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
			TileEntityUtil.sendUpdatePacket(this, super.write(compound));
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
			TileEntityUtil.sendUpdatePacket(this, super.write(compound));
		}
		this.markDirty();
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
				return trade.stockCount(this);
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
		this.writeLogger(compound);
		
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
		ItemTradeData.saveAllData(compound, this.trades);
		return compound;
	}
	
	public CompoundNBT writeLogger(CompoundNBT compound)
	{
		this.logger.write(compound);
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
		if(this.world.isRemote)
			this.rotationTime++;
	}
	
	public void AddContainerListener(ItemTraderStorageContainer container)
	{
		if(!storageContainers.contains(container))
			storageContainers.add(container);
	}
	
	public void RemoveContainerListener(ItemTraderStorageContainer container)
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
	public boolean isEmpty() {
		
		//CurrencyMod.LOGGER.info("ATMTileEntity.isEmpty().");
		for(ItemStack stack : this.inventory)
		{
			if(!stack.isEmpty())
				return false;
		}
		return true;
	}
	
	@Override
	public ItemStack getStackInSlot(int slot) {
		
		if(slot < 0 || slot >= this.inventory.size())
		{
			LightmansCurrency.LogError("ItemTraderTileEntity.getStackInSlot. Attempting to get slot " + slot + " but array size is " + this.inventory.size() + " instead of " + this.getSizeInventory());
			return ItemStack.EMPTY;
		}
		//CurrencyMod.LOGGER.info("ATMTileEntity.getStackInSlot(int slot (" + String.valueOf(slot) + ")).");
		return this.inventory.get(slot);
		
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		
		
		//CurrencyMod.LOGGER.info("ATMTileEntity.decrStackSize(int slot (" + String.valueOf(slot) + "), int amount (" + String.valueOf(amount) + ")).");
		
		int currentCount = getStackInSlot(slot).getCount();
		
		ItemStack newStack = null;
		
		//Current count is less than or equal to the requested count.
		//Return existing stack, and replace that slot with an empty ItemStack.
		if(currentCount <= amount)
		{
			newStack = getStackInSlot(slot);
			inventory.set(slot, ItemStack.EMPTY);
		}
		//Present count is greater than the requested count.
		//Create copy with requested count, and decrease the existing count by the requested amount.
		else
		{
			newStack = new ItemStack(getStackInSlot(slot).getItem(), amount);
			if(getStackInSlot(slot).hasTag())
				newStack.setTag(getStackInSlot(slot).getTag().copy());
			inventory.get(slot).setCount(currentCount - amount);
		}
		
		/* Send updates to client */
		if(!this.world.isRemote)
		{
			CompoundNBT compound = this.writeItems(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, super.write(compound));
		}
		
		return newStack;
		
	}
	
	@Override
	public ItemStack removeStackFromSlot(int slot) {
		
		//CurrencyMod.LOGGER.info("ATMTileEntity.removeStackFromSlot(int slot (" + String.valueOf(slot) + ")).");
		ItemStack slotStack = inventory.get(slot);
		inventory.set(slot, ItemStack.EMPTY);
		
		/* Send updates to client */
		if(!this.world.isRemote)
		{
			CompoundNBT compound = this.writeItems(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, super.write(compound));
		}
		
		return slotStack;
		
	}
	
	@Override
	public void setInventorySlotContents(int slot, ItemStack items) {
		
		//CurrencyMod.LOGGER.info("ATMTileEntity.setInventorySlotContents(int slot (" + String.valueOf(slot) + "), items)).");
		inventory.set(slot, items);
		
		/* Send updates to client */
		if(!this.world.isRemote)
		{	
			CompoundNBT compound = this.writeItems(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(this, super.write(compound));
		}
		
	}
	
	@Override
	public void clear() {
		
		//CurrencyMod.LOGGER.info("ATMTileEntity.clear().");
		this.inventory.clear();
		
	}
	
	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return true;
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
		return this;
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
					tradeStack = InventoryUtil.TryPutItemStack(this, tradeStack);
				if(!tradeStack.isEmpty())
					InventoryUtil.dumpContents(this.world, pos, InventoryUtil.buildInventory(tradeStack));
			}
		}
		
	}
	
}
