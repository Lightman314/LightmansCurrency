package io.github.lightman314.lightmanscurrency.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.common.ItemTraderStorageUtil;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ICreativeTraderContainer;
import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderStorageContainer;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.TicketTradeInputSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageSyncTrades;
import io.github.lightman314.lightmanscurrency.tileentity.TicketTraderTileEntity;
import io.github.lightman314.lightmanscurrency.tradedata.TicketTradeData;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class TicketTraderStorageContainer extends Container implements ITraderStorageContainer, ICreativeTraderContainer{

	public static final int SCREEN_EXTENSION = ItemTraderStorageUtil.SCREEN_EXTENSION;
	
	public final PlayerEntity player;
	
	public final TicketTraderTileEntity tileEntity;
	
	final IInventory tradeInventory;
	final IInventory coinSlots;
	final List<TicketTradeInputSlot> tradeSlots;
	
	public TicketTraderStorageContainer(int windowId, PlayerInventory inventory, TicketTraderTileEntity tileEntity)
	{
		super(ModContainers.TICKETTRADERSTORAGE, windowId);
		this.tileEntity = tileEntity;
		this.tileEntity.AddContainerListener(this);
		
		this.player = inventory.player;
		
		this.tileEntity.userOpen(this.player);
		
		int tradeCount = this.tileEntity.getTradeCount();
		int rowCount = ItemTraderStorageUtil.getRowCount(tradeCount);
		int columnCount = 9 * ItemTraderStorageUtil.getColumnCount(tradeCount);
		
		//Storage Slots
		for(int y = 0; y < rowCount; y++)
		{
			for(int x = 0; x < columnCount && x + y * columnCount < tileEntity.getSizeInventory(); x++)
			{
				this.addSlot(new Slot(tileEntity.getStorage(), x + y * columnCount, 8 + x * 18 + SCREEN_EXTENSION + ItemTraderStorageUtil.getStorageSlotOffset(tradeCount, y), 18 + y * 18));
			}
		}
		
		this.tradeInventory = new Inventory(tradeCount);
		this.tradeSlots = new ArrayList<>(tradeCount);
		//Trade Slots
		for(int y = 0; y < tradeInventory.getSizeInventory(); y++)
		{
			TicketTradeData trade = tileEntity.getTrade(y);
			TicketTradeInputSlot newSlot = new TicketTradeInputSlot(tradeInventory, y, ItemTraderStorageUtil.getTradeSlotPosX(tradeCount, y), ItemTraderStorageUtil.getTradeSlotPosY(tradeCount, y), trade, this.player);
			this.addSlot(newSlot);
			this.tradeSlots.add(newSlot);
			this.tradeInventory.setInventorySlotContents(y, trade.getTicket());
		}
		
		int inventoryOffset = ItemTraderStorageUtil.getInventoryOffset(tradeCount);
		
		//Coin slots
		this.coinSlots = new Inventory(5);
		for(int i = 0; i < 5; i++)
		{
			this.addSlot(new CoinSlot(this.coinSlots, i, inventoryOffset + 176 + 8 + SCREEN_EXTENSION, getStorageBottom() + 3 + i * 18));
		}
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, inventoryOffset + 8 + x * 18 + SCREEN_EXTENSION, getStorageBottom() + 15 + y * 18));
			}
		}
		
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, inventoryOffset + 8 + x * 18 + SCREEN_EXTENSION, getStorageBottom() + 15 + 58));
		}
		
		
		
		
	}
	
	public int getStorageBottom()
	{
		return (ItemTraderStorageUtil.getRowCount(this.tileEntity.getTradeCount()) * 18) + 28;
	}
	
	public void tick()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeScreen();
			return;
		}
		SyncTrades();
	}
	
	public void setTicketIDForSlot(int slotIndex, UUID ticketID)
	{
		int tradeIndex = slotIndex - this.tileEntity.getSizeInventory();
		setTicketIDForSlotForTrade(tradeIndex, ticketID);
	}
	
	public void setTicketIDForSlotForTrade(int tradeIndex, UUID ticketID)
	{
		this.tileEntity.getTrade(tradeIndex).setTicketID(ticketID);
		this.resyncTrades();
	}
	
	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, PlayerEntity player)
	{
		
		if(slotClickOverride(slotId, dragType, clickType, player))
		{
			this.detectAndSendChanges();
			return ItemStack.EMPTY;
		}
		
		ItemStack stack = super.slotClick(slotId, dragType, clickType, player);
		this.detectAndSendChanges();
		return stack;
		
	}
	
	private boolean slotClickOverride(int slotId, int dragType, ClickType clickType, PlayerEntity player)
	{
		//LightmansCurrency.LOGGER.info("ItemTraderStorageContainer.slotClick(" + slotId + ", " + dragType + ", " + clickType + ", " + player.getName().getString() + ")");
		if(slotId > 0 && slotId < this.inventorySlots.size())
		{
			Slot slot = this.inventorySlots.get(slotId);
			if(slot instanceof TicketTradeInputSlot && clickType != ClickType.CLONE)
			{
				//LightmansCurrency.LOGGER.info("TradeInputSlot slot clicked at slotID " + slotId);
				if(clickType == ClickType.PICKUP && (dragType == 0 || dragType == 1))
				{
					ItemStack handStack = this.player.inventory.getItemStack();
					this.setTicketIDForSlot(slotId, TicketItem.GetTicketID(handStack));
				}
				//Otherwise do nothing
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.inventorySlots.get(index);
		
		if(slot != null && slot.getHasStack())
		{
			ItemStack slotStack = slot.getStack();
			clickedStack = slotStack.copy();
			//Merge items from storage back into the players inventory
			if(index < this.tileEntity.getSizeInventory() + this.tradeInventory.getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack,  this.tileEntity.getSizeInventory() + this.tradeInventory.getSizeInventory() + this.coinSlots.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			//Merge items from the coin slots back into the players inventory
			else if(index < this.tileEntity.getSizeInventory() + this.tradeInventory.getSizeInventory() + this.coinSlots.getSizeInventory())
			{
				LightmansCurrency.LogInfo("Merging coin slots back into inventory.");
				if(!this.mergeItemStack(slotStack, this.tileEntity.getSizeInventory() + this.tradeInventory.getSizeInventory() + this.coinSlots.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else
			{
				//Merging items from the players inventory
				if(MoneyUtil.isCoin(slotStack))
				{
					//Merge coins into the coin slots
					if(!this.mergeItemStack(slotStack, this.tileEntity.getSizeInventory() + this.tradeInventory.getSizeInventory(), this.tileEntity.getSizeInventory() + this.tradeInventory.getSizeInventory() + this.coinSlots.getSizeInventory(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				//Merge everything else into the storage slots
				else if(!this.mergeItemStack(slotStack, 0, this.tileEntity.getSizeInventory(), false))
				{
					return ItemStack.EMPTY;
				}
			}
			if(slotStack.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
		}
		
		return clickedStack;
		
	}
	
	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return true;
	}
	
	@Override
	public void onContainerClosed(PlayerEntity playerIn)
	{
		//syncTrades();
		//if(!playerIn.world.isRemote)
		//	TileEntityUtil.sendUpdatePacket(this.tileEntity);
		clearContainer(playerIn, playerIn.world, coinSlots);
		
		this.tileEntity.RemoveContainerListener(this);
		
		super.onContainerClosed(playerIn);
		
		
		
		this.tileEntity.userClose(this.player);
		
	}
	
	public void SyncTrades()
	{
		boolean changed = false;
		boolean isServer = !player.world.isRemote;
		for(int i = 0; i < tileEntity.getTradeCount(); i++)
		{
			UUID tradeID = tileEntity.getTrade(i).getTicketID();
			UUID currentID = TicketItem.GetTicketID(this.tradeInventory.getStackInSlot(i));
			if(tradeID == null)
			{
				if(currentID != null)
				{
					changed = true;
					tileEntity.getTrade(i).setTicketID(currentID);
				}
			}
			else
			{
				if(currentID == null)
				{
					changed = true;
					tileEntity.getTrade(i).setTicketID(currentID);
				}
				else if(!tradeID.equals(currentID))
				{
					changed = true;
					tileEntity.getTrade(i).setTicketID(currentID);
				}
			}
		}
		if(changed && isServer)
		{
			//Change detected server-side, so send an update packet to the relevant clients.
			CompoundNBT compound = this.tileEntity.writeTrades(new CompoundNBT());
			TileEntityUtil.sendUpdatePacket(tileEntity, this.tileEntity.superWrite(compound));
		}
		else if(changed)
		{
			//Change was detected client-side, so inform the server that it needs to check for changes.
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSyncTrades());
		}
	}
	
	public void resyncTrades()
	{
		for(int i = 0; i < tradeInventory.getSizeInventory(); i++)
		{
			TicketTradeData trade = this.tileEntity.getTrade(i);
			if(trade != null)
			{
				tradeInventory.setInventorySlotContents(i, trade.getTicket());
			}
			else
			{
				tradeInventory.setInventorySlotContents(i, ItemStack.EMPTY);
			}
		}
	}
	
	public boolean isOwner()
	{
		return tileEntity.isOwner(player);
	}
	
	public boolean HasCoinsToAdd()
	{
		return !coinSlots.isEmpty();
	}
	
	public void AddCoins()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeScreen();
			return;
		}
		//Get the value of the current 
		CoinValue addValue = CoinValue.easyBuild2(this.coinSlots);
		this.tileEntity.addStoredMoney(addValue);
		this.coinSlots.clear();
	}
	
	public void CollectCoinStorage()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeScreen();
			return;
		}
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(tileEntity.getStoredMoney());
		ItemStack wallet = LightmansCurrency.getWalletStack(this.player);
		if(!wallet.isEmpty())
		{
			List<ItemStack> spareCoins = new ArrayList<>();
			for(int i = 0; i < coinList.size(); i++)
			{
				ItemStack extraCoins = WalletItem.PickupCoin(wallet, coinList.get(i));
				if(!extraCoins.isEmpty())
					spareCoins.add(extraCoins);
			}
			coinList = spareCoins;
		}
		IInventory inventory = new Inventory(coinList.size());
		for(int i = 0; i < coinList.size(); i++)
		{
			inventory.setInventorySlotContents(i, coinList.get(i));
		}
		this.clearContainer(player, player.getEntityWorld(), inventory);
		
		//Clear the coin storage
		tileEntity.clearStoredMoney();
		
	}
	
	public void ToggleCreative()
	{
		if(this.tileEntity.isRemoved())
		{
			this.player.closeScreen();
			return;
		}
		this.tileEntity.toggleCreative();
	}

	@Override
	public void AddTrade() {
		this.tileEntity.addTrade();
	}

	@Override
	public void RemoveTrade() {
		this.tileEntity.removeTrade();
	}
	
}
