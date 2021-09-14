package io.github.lightman314.lightmanscurrency.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.containers.interfaces.ITraderContainerPrimitive;
import io.github.lightman314.lightmanscurrency.containers.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.containers.slots.TicketSlot;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.WalletUtil;
import io.github.lightman314.lightmanscurrency.util.WalletUtil.PlayerWallets;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import io.github.lightman314.lightmanscurrency.blockentity.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;

public class PaygateContainer extends AbstractContainerMenu implements ITraderContainerPrimitive{
	
	public final Player player;
	
	protected final Container coinInput = new SimpleContainer(5);
	protected final Container ticketInput = new SimpleContainer(1);
	public final PaygateBlockEntity blockEntity;
	
	public final int priceInputOffset;
	
	public PaygateContainer(int windowId, Inventory inventory, PaygateBlockEntity tileEntity)
	{
		super(ModContainers.PAYGATE, windowId);
		this.blockEntity = tileEntity;
		
		this.player = inventory.player;
		
		this.priceInputOffset = this.isOwner() ? CoinValueInput.HEIGHT : 0;
		
		//Coinslots
		for(int x = 0; x < coinInput.getContainerSize(); x++)
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
	public boolean stillValid(Player playerIn)
	{
		//return this.callable.applyOrElse((world,pos) -> playerIn.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
		//return this.tileEntity.isUsableByPlayer(playerIn);
		return true;
	}
	
	@Override
	public void removed(Player playerIn)
	{
		super.removed(playerIn);
		this.clearContainer(playerIn, this.coinInput);
		this.clearContainer(playerIn, this.ticketInput);
	}

	@Override
	public ItemStack quickMoveStack(Player playerEntity, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			if(index < this.coinInput.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack,  this.coinInput.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index >= this.coinInput.getContainerSize() && index < this.coinInput.getContainerSize() + this.ticketInput.getContainerSize())
			{
				if(!this.moveItemStackTo(slotStack, this.coinInput.getContainerSize() + this.ticketInput.getContainerSize(), this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(index < this.slots.size())
			{
				//if(!MoneyUtil.isCoin(slotStack.getItem()))
				//	return ItemStack.EMPTY;
				if(!this.moveItemStackTo(slotStack, 0, this.coinInput.getContainerSize() + this.ticketInput.getContainerSize(), false))
				{
					return ItemStack.EMPTY;
				}
			}
			
			if(slotStack.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else
			{
				slot.setChanged();
			}
		}
		
		return clickedStack;
		
	}
	
	public boolean HasMasterTicket()
	{
		return TicketItem.isMasterTicket(ticketInput.getItem(0));
	}
	
	public boolean HasValidTicket()
	{
		//Get the ticket item
		ItemStack ticket = ticketInput.getItem(0);
		//Cannot consume master tickets
		if(TicketItem.isMasterTicket(ticket))
			return false;
		return this.blockEntity.validTicket(TicketItem.GetTicketID(ticket));
	}
	
	public UUID GetTicketID()
	{
		return TicketItem.GetTicketID(ticketInput.getItem(0));
	}
	
	public long GetCoinValue()
	{
		long value = 0;
		for(int i = 0; i < coinInput.getContainerSize(); i++)
		{
			value += MoneyUtil.getValue(coinInput.getItem(i));
		}
		value += WalletUtil.getWallets(this.player).getStoredMoney();
		return value;
	}
	
	public void Activate()
	{
		
		//Check if a valid ticket is present
		if(HasValidTicket())
		{
			//Remove the ticket
			ticketInput.removeItem(0, 1);
		}
		else
		{
			//Process the payment via MoneyUtil
			if(!MoneyUtil.ProcessPayment(this.coinInput, this.player, this.blockEntity.getPrice()))
				return;
			
			//Add the money to storage
			this.blockEntity.addStoredMoney(this.blockEntity.getPrice());
			
		}
		
		//Activate the redstone signal
		this.blockEntity.activate();
		
	}
	
	@Override
	public void CollectCoinStorage()
	{
		List<ItemStack> coinList = MoneyUtil.getCoinsOfValue(blockEntity.getStoredMoney());
		PlayerWallets wallet = WalletUtil.getWallets(this.player);
		if(wallet.hasWallet())
		{
			List<ItemStack> spareCoins = new ArrayList<>();
			for(int i = 0; i < coinList.size(); i++)
			{
				ItemStack extraCoins = wallet.PlaceCoin(coinList.get(i));
				if(!extraCoins.isEmpty())
					spareCoins.add(extraCoins);
			}
			coinList = spareCoins;
		}
		for(int i = 0; i < coinList.size(); i++)
		{
			if(!manualCoinMerge(coinList.get(i)))
			{
				Container inventory = new SimpleContainer(1);
				inventory.setItem(0, coinList.get(i));
				this.clearContainer(player, inventory);
			}
		}
		//Clear the coin storage
		blockEntity.clearStoredMoney();
		
	}
	
	private boolean manualCoinMerge(ItemStack mergeStack)
	{
		int amountToMerge = mergeStack.getCount();
		Item mergeItem = mergeStack.getItem();
		List<Pair<Integer,Integer>> mergeOrders = new ArrayList<>();
		//First pass, checking for other stacks to add to
		for(int i = 0; i < coinInput.getContainerSize() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = coinInput.getItem(i);
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
		for(int i = 0; i < coinInput.getContainerSize() && amountToMerge > 0; i++)
		{
			ItemStack inventoryStack = coinInput.getItem(i);
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
			ItemStack itemStack = coinInput.getItem(order.getFirst());
			if(itemStack.isEmpty())
			{
				coinInput.setItem(order.getFirst(), new ItemStack(mergeItem, order.getSecond()));
			}
			else
			{
				coinInput.setItem(order.getFirst(),  new ItemStack(mergeItem, order.getSecond() + itemStack.getCount()));
			}
		});
		
		return true;
	}
	
	public boolean isOwner()
	{
		return blockEntity.isOwner(player);
	}
	
}
