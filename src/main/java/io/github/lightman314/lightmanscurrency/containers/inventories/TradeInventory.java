package io.github.lightman314.lightmanscurrency.containers.inventories;

import io.github.lightman314.lightmanscurrency.ItemTradeData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class TradeInventory implements IInventory{

	NonNullList<ItemTradeData> tradeData;
	
	public TradeInventory(NonNullList<ItemTradeData> tradeData)
	{
		this.tradeData = tradeData;
	}
	
	public ItemStack getStackInSlot(int slot)
	{
		return tradeData.get(slot).getSellItem();
	}
	
	public int getSizeInventory()
	{
		return tradeData.size();
	}
	
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		tradeData.get(slot).setSellItem(stack);
	}
	
	public ItemStack decrStackSize(int slot, int amount)
	{
		
		int currentCount = getStackInSlot(slot).getCount();
		
		ItemStack newStack = null;
		
		//Current count is less than or equal to the requested count.
		//Return existing stack, and replace that slot with an empty ItemStack.
		if(currentCount <= amount)
		{
			newStack = getStackInSlot(slot);
			tradeData.get(slot).setSellItem(ItemStack.EMPTY);
			//inventory.set(slot, ItemStack.EMPTY);
		}
		//Present count is greater than the requested count.
		//Create copy with requested count, and decrease the existing count by the requested amount.
		else
		{
			newStack = new ItemStack(getStackInSlot(slot).getItem(), amount, getStackInSlot(slot).getTag());
			tradeData.get(slot).getSellItem().setCount(currentCount - amount);
		}
		
		return newStack;
	}
	
	public boolean isEmpty()
	{
		for(ItemTradeData trade : this.tradeData)
		{
			if(!trade.getSellItem().isEmpty())
				return false;
		}
		return true;
	}
	
	public boolean isUsableByPlayer(PlayerEntity player)
	{
		return true;
	}
	
	public void markDirty()
	{
		
	}
	
	public ItemStack removeStackFromSlot(int slot)
	{
		ItemStack slotStack = getStackInSlot(slot);
		setInventorySlotContents(slot, ItemStack.EMPTY);
		return slotStack;
		
	}
	
	public void clear()
	{
		for(ItemTradeData trade : tradeData)
		{
			trade.setSellItem(ItemStack.EMPTY);
		}
	}
	
}
