package io.github.lightman314.lightmanscurrency.trader.tradedata;


import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

public class ItemTradeData extends TradeData {
	
	public enum ItemTradeRestrictions { NONE, ARMOR_HEAD, ARMOR_CHEST, ARMOR_LEGS, ARMOR_FEET, TICKET }
	public enum ItemTradeType { SALE, PURCHASE, BARTER }
	
	public static int MaxTradeTypeStringLength()
	{ 
		int length = 0;
		for(ItemTradeType value : ItemTradeType.values())
		{
			int thisLength = value.name().length();
			if(thisLength > length)
				length = thisLength;
		}
		return length;
	}
	
	public static final int MAX_CUSTOMNAME_LENGTH = 30;
	
	ItemTradeRestrictions restriction = ItemTradeRestrictions.NONE;
	ItemStack sellItem = ItemStack.EMPTY;
	ItemStack barterItem = ItemStack.EMPTY;
	ItemTradeType tradeType = ItemTradeType.SALE;
	String customName = "";
	
	public ItemStack getSellItem()
	{
		return this.sellItem.copy();
	}
	
	public ItemStack getBarterItem()
	{
		return this.barterItem.copy();
	}
	
	public void setSellItem(ItemStack itemStack)
	{
		if(this.restriction == ItemTradeRestrictions.TICKET)
		{
			//Only let it define the ticket via master ticket to confirm that the ticket is being made by the owner.
			if(TicketItem.isMasterTicket(itemStack))
			{
				UUID ticketID = TicketItem.GetTicketID(itemStack);
				if(ticketID == null)
					this.sellItem = ItemStack.EMPTY;
				else
					this.sellItem = TicketItem.CreateTicket(ticketID, 1);
			}
			//Allow ticket kiosks to buy or sell ticket materials such as paper & ticket stubs
			//Manually blacklist tickets
			else if(itemStack.getItem().getTags().contains(TicketItem.TICKET_MATERIAL_TAG) && itemStack.getItem() != ModItems.TICKET)
			{
				this.sellItem = itemStack;
			}
			else
				this.sellItem = ItemStack.EMPTY;
		}
		else
			this.sellItem = itemStack.copy();
	}
	
	public void setBarterItem(ItemStack itemStack)
	{
		this.barterItem = itemStack.copy();
	}
	
	public String getCustomName()
	{
		return this.customName;
	}
	
	public void setCustomName(String customName)
	{
		this.customName = customName;
	}
	
	@Override
	public TradeDirection getTradeDirection()
	{
		switch(this.tradeType)
		{
		case SALE:
			return TradeDirection.SALE;
		case PURCHASE:
			return TradeDirection.PURCHASE;
			default:
				return TradeDirection.NONE;
		}
	}
	
	public ItemTradeType getTradeType() { return this.tradeType; }
	
	public boolean isSale() { return this.tradeType == ItemTradeType.SALE; }
	public boolean isPurchase() { return this.tradeType == ItemTradeType.PURCHASE; }
	public boolean isBarter() { return this.tradeType == ItemTradeType.BARTER; }
	
	public void setTradeType(ItemTradeType tradeDirection)
	{
		this.tradeType = tradeDirection;
	}
	
	public int getSlotCount()
	{
		return this.tradeType == ItemTradeType.BARTER ? 2 : 1;
	}
	
	public ItemTradeRestrictions getRestriction()
	{
		return this.restriction;
	}
	
	public void setRestriction(ItemTradeRestrictions restriction)
	{
		this.restriction = restriction;
	}
	
	public static EquipmentSlotType getSlotFromRestriction(ItemTradeRestrictions restriction)
	{
		if(restriction == ItemTradeRestrictions.ARMOR_HEAD)
			return EquipmentSlotType.HEAD;
		if(restriction == ItemTradeRestrictions.ARMOR_CHEST)
			return EquipmentSlotType.CHEST;
		if(restriction == ItemTradeRestrictions.ARMOR_LEGS)
			return EquipmentSlotType.LEGS;
		if(restriction == ItemTradeRestrictions.ARMOR_FEET)
			return EquipmentSlotType.FEET;
		
		return null;
	}
	
	@Override
	public boolean isValid()
	{
		if(this.tradeType == ItemTradeType.BARTER)
			return !this.sellItem.isEmpty() && !this.barterItem.isEmpty();
		return super.isValid() && !this.sellItem.isEmpty();
	}
	
	public boolean hasStock(IItemTrader trader)
	{
		if(this.sellItem.isEmpty())
			return false;
		return stockCount(trader) > 0;
	}
	
	public boolean hasSpace(IItemTrader trader)
	{
		switch(this.tradeType)
		{
		case PURCHASE:
			return InventoryUtil.CanPutItemStack(trader.getStorage(), this.getSellItem());
		case BARTER:
			return InventoryUtil.CanPutItemStack(trader.getStorage(), this.getBarterItem());
			default:
				return true;
		}
	}
	
	public int stockCount(IItemTrader trader)
	{
		if(this.sellItem.isEmpty())
			return 0;
		
		if(this.tradeType == ItemTradeType.PURCHASE)
		{
			if(this.isFree)
				return 1;
			if(this.cost.getRawValue() == 0)
				return 0;
			long coinValue = trader.getStoredMoney().getRawValue();
			long price = this.cost.getRawValue();
			return (int)(coinValue / price);
		}
		else if(this.tradeType == ItemTradeType.SALE || this.tradeType == ItemTradeType.BARTER)
		{
			if(this.restriction == ItemTradeRestrictions.TICKET)
			{
				return InventoryUtil.GetItemTagCount(trader.getStorage(), TicketItem.TICKET_MATERIAL_TAG) / this.sellItem.getCount();
			}
			else
			{
				return InventoryUtil.GetItemCount(trader.getStorage(), this.sellItem) / this.sellItem.getCount();
			}
		}
		else //Other types are not handled yet.
			return 0;
	}
	
	public void RemoveItemsFromStorage(IInventory storage)
	{
		if(this.restriction == ItemTradeRestrictions.TICKET)
		{
			if(!InventoryUtil.RemoveItemCount(storage, this.getSellItem()))
			{
				InventoryUtil.RemoveItemTagCount(storage, TicketItem.TICKET_MATERIAL_TAG, this.sellItem.getCount());
			}
		}
		else
			InventoryUtil.RemoveItemCount(storage, this.getSellItem());
	}
	
	@Override
	public CompoundNBT getAsNBT() {
		CompoundNBT tradeNBT = super.getAsNBT();
		CompoundNBT sellItemCompound = new CompoundNBT();
		sellItem.write(sellItemCompound);
		tradeNBT.put("SellItem", sellItemCompound);
		CompoundNBT barterItemCompound = new CompoundNBT();
		barterItem.write(barterItemCompound);
		tradeNBT.put("BarterItem", barterItemCompound);
		tradeNBT.putString("TradeDirection", this.tradeType.name());
		tradeNBT.putString("Restrictions", this.restriction.name());
		tradeNBT.putString("CustomName", this.customName);
		return tradeNBT;
	}
	
	public static CompoundNBT saveAllData(CompoundNBT nbt, NonNullList<ItemTradeData> data)
	{
		return saveAllData(nbt, data, DEFAULT_KEY);
	}
	
	public static CompoundNBT saveAllData(CompoundNBT nbt, NonNullList<ItemTradeData> data, String key)
	{
		ListNBT listNBT = new ListNBT();
		
		for(int i = 0; i < data.size(); i++)
		{
			listNBT.add(data.get(i).getAsNBT());
		}
		
		if(listNBT.size() > 0)
			nbt.put(key, listNBT);
		
		return nbt;
	}
	
	public static NonNullList<ItemTradeData> loadAllData(CompoundNBT nbt, int arraySize)
	{
		return loadAllData(DEFAULT_KEY, nbt, arraySize);
	}
	
	public static NonNullList<ItemTradeData> loadAllData(String key, CompoundNBT nbt, int arraySize)
	{
		ListNBT listNBT = nbt.getList(key, Constants.NBT.TAG_COMPOUND);
		
		NonNullList<ItemTradeData> data = listOfSize(arraySize);
		
		for(int i = 0; i < listNBT.size() && i < arraySize; i++)
		{
			//CompoundNBT compoundNBT = listNBT.getCompound(i);
			data.get(i).loadFromNBT(listNBT.getCompound(i));
		}
		
		return data;
	}
	
	@Override
	public void loadFromNBT(CompoundNBT nbt)
	{
		
		super.loadFromNBT(nbt);
		
		//Load the Sell Item
		if(nbt.contains("SellItem", Constants.NBT.TAG_COMPOUND))
			sellItem = ItemStack.read(nbt.getCompound("SellItem"));
		else //Load old format from before the bartering system was made
			sellItem = ItemStack.read(nbt);
		
		//Load the Barter Item
		if(nbt.contains("BarterItem", Constants.NBT.TAG_COMPOUND))
			barterItem = ItemStack.read(nbt.getCompound("BarterItem"));
		else
			barterItem = ItemStack.EMPTY;
		
		//Set the Trade Direction
		if(nbt.contains("TradeDirection", Constants.NBT.TAG_STRING))
			this.tradeType = loadTradeType(nbt.getString("TradeDirection"));
		else
			this.tradeType = ItemTradeType.SALE;
		
		//Set the restrictions
		if(nbt.contains("Restrictions"))
			this.restriction = loadRestriction(nbt.getString("Restrictions"));
		else
			this.restriction = ItemTradeRestrictions.NONE;
		
		if(nbt.contains("CustomName"))
			this.customName = nbt.getString("CustomName");
		else
			this.customName = "";
	}
	
	private static ItemTradeRestrictions loadRestriction(String name)
	{
		ItemTradeRestrictions value = ItemTradeRestrictions.NONE;
		try {
			value = ItemTradeRestrictions.valueOf(name);
		}
		catch (IllegalArgumentException exception)
		{
			LightmansCurrency.LogError("Could not load '" + name + "' as a TradeRestriction.");
		}
		return value;
	}
	
	public static ItemTradeType loadTradeType(String name)
	{
		ItemTradeType value = ItemTradeType.SALE;
		try {
			value = ItemTradeType.valueOf(name);
		}
		catch (IllegalArgumentException exception)
		{
			LightmansCurrency.LogError("Could not load '" + name + "' as a TradeDirection.");
		}
		return value;
	}
	
	public static NonNullList<ItemTradeData> listOfSize(int tradeCount)
	{
		NonNullList<ItemTradeData> data = NonNullList.withSize(tradeCount, new ItemTradeData());
		for(int i = 0; i < tradeCount; i++)
		{
			data.set(i, new ItemTradeData());
		}
		return data;
	}
	
	public void markRulesDirty() { }
	
}
