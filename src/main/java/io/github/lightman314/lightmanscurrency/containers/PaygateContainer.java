package io.github.lightman314.lightmanscurrency.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderContainerPrimitive;
import io.github.lightman314.lightmanscurrency.containers.inventories.TicketInventory;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.TicketSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.tileentity.PaygateTileEntity;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PaygateContainer extends Container implements ITraderContainerPrimitive{
	
	public final PlayerEntity player;
	
	protected static final ContainerType<?> type = ModContainers.ITEMTRADER;
	
	protected final IInventory coinInput = new Inventory(5);
	protected final IInventory ticketInput = new TicketInventory(1);
	public final PaygateTileEntity tileEntity;
	
	public final int priceInputOffset;
	
	public PaygateContainer(int windowId, PlayerInventory inventory, PaygateTileEntity tileEntity)
	{
		super(ModContainers.PAYGATE, windowId);
		this.tileEntity = tileEntity;
		
		this.player = inventory.player;
		
		this.priceInputOffset = this.isOwner() ? CoinValueInput.HEIGHT : 0;
		
		//Coinslots
		for(int x = 0; x < coinInput.getSizeInventory(); x++)
		{
			this.addSlot(new CoinSlot(this.coinInput, x, 8 + (x + 4) * 18, 37 + this.priceInputOffset));
		}
		
		//Ticket Slot
		this.addSlot(new TicketSlot(this.ticketInput, 0, 8 + (3 * 18), 37 + this.priceInputOffset));
		
		//Player inventory
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 69 + y * 18 + this.priceInputOffset));
			}
		}
		
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, 8 + x * 18, 127 + this.priceInputOffset));
		}
		
	}
	
	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		//return this.callable.applyOrElse((world,pos) -> playerIn.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
		//return this.tileEntity.isUsableByPlayer(playerIn);
		return true;
	}
	
	@Override
	public void onContainerClosed(PlayerEntity playerIn)
	{
		super.onContainerClosed(playerIn);
		this.clearContainer(playerIn,  playerIn.world,  this.coinInput);
		this.clearContainer(playerIn, playerIn.world, this.ticketInput);
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
			if(index < this.coinInput.getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack,  this.coinInput.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index >= this.coinInput.getSizeInventory() && index < this.coinInput.getSizeInventory() + this.ticketInput.getSizeInventory())
			{
				if(!this.mergeItemStack(slotStack, this.coinInput.getSizeInventory() + this.ticketInput.getSizeInventory(), this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index < this.inventorySlots.size())
			{
				//if(!MoneyUtil.isCoin(slotStack.getItem()))
				//	return ItemStack.EMPTY;
				if(!this.mergeItemStack(slotStack, 0, this.coinInput.getSizeInventory() + this.ticketInput.getSizeInventory(), false))
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
	
	public boolean HasMasterTicket()
	{
		return TicketItem.isMasterTicket(ticketInput.getStackInSlot(0));
	}
	
	public boolean HasValidTicket()
	{
		//Get the ticket item
		ItemStack ticket = ticketInput.getStackInSlot(0);
		//Cannot consume master tickets
		if(TicketItem.isMasterTicket(ticket))
			return false;
		return this.tileEntity.validTicket(ticket);
	}
	
	public UUID GetTicketID()
	{
		return TicketItem.GetTicketID(ticketInput.getStackInSlot(0));
	}
	
	public long GetCoinValue()
	{
		long value = 0;
		for(int i = 0; i < coinInput.getSizeInventory(); i++)
		{
			value += MoneyUtil.getValue(coinInput.getStackInSlot(i));
		}
		ItemStack wallet = LightmansCurrency.getWalletStack(this.player);
		if(!wallet.isEmpty())
		{
			value += MoneyUtil.getValue(WalletItem.getWalletInventory(wallet));
		}
		return value;
	}
	
	public boolean CanActivate()
	{
		if(this.tileEntity.isActive())
			return false;
		if(this.tileEntity.HasPairedTicket())
		{
			if(this.tileEntity.getPrice().getRawValue() <= 0)
				return this.HasValidTicket();
			else
				return this.HasValidTicket() || this.GetCoinValue() >= this.tileEntity.getPrice().getRawValue();
		}
		else
		{
			return this.GetCoinValue() >= this.tileEntity.getPrice().getRawValue();
		}
	}
	
	public void Activate()
	{
		
		if(!CanActivate())
			return;
		
		//Check if a valid ticket is present
		if(HasValidTicket())
		{
			//Remove the ticket
			ticketInput.decrStackSize(0, 1);
			//Generate a ticket stub
			ItemStack ticketStub = new ItemStack(ModItems.TICKET_STUB);
			//Try to put it in the ticket slot
			if(ticketInput.getStackInSlot(0).isEmpty())
				ticketInput.setInventorySlotContents(0, ticketStub);
			else
			{
				//Otherwise force it into the players inventory
				IInventory temp = new Inventory(1);
				temp.setInventorySlotContents(0, ticketStub);
				this.clearContainer(this.player, this.player.world, temp);
			}
		}
		else
		{
			//Process the payment via MoneyUtil
			if(!MoneyUtil.ProcessPayment(this.coinInput, this.player, this.tileEntity.getPrice()))
				return;
			
			//Add the money to storage
			this.tileEntity.addStoredMoney(this.tileEntity.getPrice());
			
		}
		
		//Activate the redstone signal
		this.tileEntity.activate();
		
	}
	
	@Override
	public void CollectCoinStorage()
	{
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
		for(int i = 0; i < coinList.size(); i++)
		{
			if(!manualCoinMerge(coinList.get(i)))
			{
				IInventory inventory = new Inventory(1);
				inventory.setInventorySlotContents(0, coinList.get(i));
				this.clearContainer(player, player.getEntityWorld(), inventory);
			}
		}
		//Clear the coin storage
		tileEntity.clearStoredMoney();
		
	}
	
	private boolean manualCoinMerge(ItemStack mergeStack)
	{
		int amountToMerge = mergeStack.getCount();
		Item mergeItem = mergeStack.getItem();
		List<Pair<Integer,Integer>> mergeOrders = new ArrayList<>();
		//First pass, checking for other stacks to add to
		for(int i = 0; i < coinInput.getSizeInventory() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = coinInput.getStackInSlot(i);
			if(inventoryStack.getItem() == mergeItem && inventoryStack.getCount() != inventoryStack.getMaxStackSize())
			{
				int availableSlots = inventoryStack.getMaxStackSize() - inventoryStack.getCount();
				int amountToPlace = amountToMerge;
				if(amountToPlace > availableSlots)
					amountToPlace = availableSlots;
				//Define the orders
				mergeOrders.add(new Pair<Integer,Integer>(i,amountToPlace));
				//Update the pending merge count
				amountToMerge -= amountToPlace;
			}
		}
		//Second pass, checking for empty slots to place them in
		for(int i = 0; i < coinInput.getSizeInventory() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = coinInput.getStackInSlot(i);
			if(inventoryStack.isEmpty())
			{
				int availableSlots = 64;
				int amountToPlace = amountToMerge;
				if(amountToPlace > availableSlots)
					amountToPlace = availableSlots;
				//Define the orders
				mergeOrders.add(new Pair<Integer,Integer>(i,amountToPlace));
				//Update the pending merge count
				amountToMerge -= amountToPlace;
			}
		}
		
		//Confirm that all items have a placed to be merged
		if(amountToMerge > 0)
			return false;
		//Execute item placement/addition
		mergeOrders.forEach(order ->
		{
			ItemStack itemStack = coinInput.getStackInSlot(order.getFirst());
			if(itemStack.isEmpty())
			{
				coinInput.setInventorySlotContents(order.getFirst(), new ItemStack(mergeItem, order.getSecond()));
			}
			else
			{
				coinInput.setInventorySlotContents(order.getFirst(),  new ItemStack(mergeItem, order.getSecond() + itemStack.getCount()));
			}
		});
		
		return true;
	}
	
	public boolean isOwner()
	{
		return tileEntity.isOwner(player);
	}
	
}
