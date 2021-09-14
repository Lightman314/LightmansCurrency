package io.github.lightman314.lightmanscurrency.blockentity;

import java.util.ArrayList;
import java.util.List;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.blocks.IItemTraderBlock;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.interfaces.ITradeButtonStockSource;
import io.github.lightman314.lightmanscurrency.containers.ItemEditContainer;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainer;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderContainerCR;
import io.github.lightman314.lightmanscurrency.containers.ItemTraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.interfaces.IItemTrader;
import io.github.lightman314.lightmanscurrency.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.ItemStackHelper;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ItemShopLogger;
import io.github.lightman314.lightmanscurrency.ItemTradeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class ItemTraderBlockEntity extends TraderBlockEntity implements ITradeButtonStockSource, IItemTrader{
	
	public static final int TRADELIMIT = 16;
	public static final int VERSION = 1;
	
	protected Container inventory;
	
	protected int tradeCount = 1;
	
	private long rotationTime = 0;
	
	protected NonNullList<ItemTradeData> trades;
	
	List<ItemTraderStorageContainer> storageContainers = new ArrayList<>();
	
	public final ItemShopLogger logger = new ItemShopLogger();
	
	public ItemTraderBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.ITEM_TRADER, pos, state);
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.inventory = new SimpleContainer(this.storageSize());
	}
	
	public ItemTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount)
	{
		super(ModBlockEntities.ITEM_TRADER, pos, state);
		this.tradeCount = tradeCount;
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.inventory = new SimpleContainer(this.storageSize());
	}
	
	protected ItemTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.inventory = new SimpleContainer(this.storageSize());
	}
	
	protected ItemTraderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int tradeCount)
	{
		super(type, pos, state);
		this.tradeCount = tradeCount;
		this.trades = ItemTradeData.listOfSize(tradeCount);
		this.inventory = new SimpleContainer(this.storageSize());
	}
	
	public void restrictTrade(int index, ItemTradeData.TradeRestrictions restriction)
	{
		getTrade(index).setRestriction(restriction);
		this.setChanged();
	}
	
	protected int storageSize()
	{
		return this.getTradeCount() * 9;
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
		this.setChanged();
	}
	
	public void removeTrade()
	{
		if(this.level.isClientSide)
			return;
		if(tradeCount <= 1)
			return;
		overrideTradeCount(tradeCount - 1);
		forceReOpen();
		this.setChanged();
	}
	
	private void forceReOpen()
	{
		for(Player player : this.getUsers())
		{
			ServerPlayer serverPlayer = (ServerPlayer)player;
			if(player.containerMenu instanceof ItemTraderStorageContainer)
				this.openStorageMenu(serverPlayer);
			else if(player.containerMenu instanceof ItemTraderContainerCR)
				this.openCashRegisterTradeMenu(serverPlayer, ((ItemTraderContainerCR)player.containerMenu).cashRegister);
			else if(player.containerMenu instanceof ItemTraderContainer)
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
		Container oldInventory = this.inventory;
		this.inventory = new SimpleContainer(this.storageSize());
		for(int i = 0; i < this.inventory.getContainerSize() && i < oldInventory.getContainerSize(); i++)
		{
			this.inventory.setItem(i, oldInventory.getItem(i));
		}
		//Attempt to place lost items into the available slots
		if(oldInventory.getContainerSize() > this.storageSize())
		{
			for(int i = this.inventory.getContainerSize(); i < oldInventory.getContainerSize(); i++)
			{
				InventoryUtil.TryPutItemStack(this.inventory, oldInventory.getItem(i));
			}
		}
		//Send an update to the client
		if(!this.level.isClientSide)
		{
			//Send update packet
			CompoundTag compound = this.writeTrades(new CompoundTag());
			this.writeItems(compound);
			TileEntityUtil.sendUpdatePacket(this, super.save(compound));
			this.setChanged();
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
			TileEntityUtil.sendUpdatePacket(this, super.save(compound));
			this.setChanged();
		}
	}
	
	public void markLoggerDirty()
	{
		//Send an update to the client
		if(!this.level.isClientSide)
		{
			//Send update packet
			CompoundTag compound = this.writeLogger(new CompoundTag());
			TileEntityUtil.sendUpdatePacket(this, super.save(compound));
			this.setChanged();
		}
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
				return trade.stockCount(this.inventory);
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
		
		return super.save(compound);
		
	}
	
	protected CompoundTag writeItems(CompoundTag compound)
	{
		ItemStackHelper.saveAllItems("Items", compound, this.inventory);
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
	
	@Override
	public void load(CompoundTag compound)
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
		if(compound.contains("Items"))
		{
			this.inventory = new SimpleContainer(this.storageSize());
			ItemStackHelper.loadAllItems("Items", compound, this.inventory);
		}
		else if(this.inventory == null)
			this.inventory = new SimpleContainer(this.storageSize());
		
		this.logger.read(compound);
		
		super.load(compound);
		
	}
	
	@Override
	public void dumpContents(Level world, BlockPos pos)
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
	public AABB getRenderBoundingBox()
	{
		return new AABB(getBlockPos().offset(-1, 0, -1), getBlockPos().offset(2,2,2));
	}

	@Override
	public void clientTick()
	{
		super.clientTick();
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
			return new ItemTraderContainer(id, inventory, tileEntity);
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
			return new ItemTraderStorageContainer(id, inventory, tileEntity);
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
			return new ItemTraderContainerCR(id, inventory, tileEntity, registerEntity);
		}
		
	}
	
	private class ItemEditContainerProvider implements MenuProvider{

		ItemTraderBlockEntity blockEntity;
		int tradeIndex;
		
		public ItemEditContainerProvider(ItemTraderBlockEntity blockEntity, int tradeIndex)
		{
			this.blockEntity = blockEntity;
			this.tradeIndex = tradeIndex;
		}
		
		public Component getDisplayName()
		{
			return blockEntity.getName();
		}
		
		public AbstractContainerMenu createMenu(int id, Inventory inventory, Player entity)
		{
			return new ItemEditContainer(id, inventory, () -> blockEntity, tradeIndex);
		}
		
	}

	@Override
	public Container getStorage() {
		return this.inventory;
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
					tradeStack = InventoryUtil.TryPutItemStack(this.inventory, tradeStack);
				if(!tradeStack.isEmpty())
					InventoryUtil.spawnItemStack(this.level, this.worldPosition, tradeStack);
			}
		}
		
	}
	
}
