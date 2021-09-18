package io.github.lightman314.lightmanscurrency.tradedata;


import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
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
	ItemTradeType tradeDirection = ItemTradeType.SALE;
	String customName = "";
	
	public ItemStack getSellItem()
	{
		return this.sellItem.copy();
	}
	
	public ItemStack getDisplayItem(IInventory storage, boolean isCreative, CoinValue storedMoney)
	{
		ItemStack displayItem = getSellItem();
		//Get the item tag
		CompoundNBT itemTag = displayItem.getOrCreateTag();
		if(this.tradeDirection == ItemTradeType.PURCHASE)
		{
			//No custom names for purchases
			if(this.cost.getRawValue() != 0)
			{
				itemTag.putBoolean("LC_DisplayItem", true);
				itemTag.putInt("LC_StockAmount", isCreative ? -1 : (int)this.tradesPossibleWithStoredMoney(storedMoney));
			}
		}
		else
		{
			itemTag.putBoolean("LC_DisplayItem", true);
			//Always to custom name data first, as it will re-load the item tag after setting the name.
			if(this.customName != "")
				itemTag.putString("LC_CustomName", this.customName);
			//Get amount in stock
			itemTag.putInt("LC_StockAmount", isCreative ? -1 : this.stockCount(storage));
		}
		displayItem.setTag(itemTag);
		return displayItem;
	}
	
	public void setSellItem(ItemStack itemStack)
	{
		if(this.restriction == ItemTradeRestrictions.TICKET)
		{
			UUID ticketID = TicketItem.GetTicketID(itemStack);
			if(ticketID == null)
				this.sellItem = ItemStack.EMPTY;
			else
				this.sellItem = TicketItem.CreateTicket(ticketID, 1);
		}
		else
			this.sellItem = itemStack.copy();
	}
	
	public String getCustomName()
	{
		return this.customName;
	}
	
	public void setCustomName(String customName)
	{
		this.customName = customName;
	}
	
	public ItemTradeType getTradeDirection()
	{
		return this.tradeDirection;
	}
	
	public void setTradeDirection(ItemTradeType tradeDirection)
	{
		this.tradeDirection = tradeDirection;
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
	
	public boolean isValid()
	{
		return !this.sellItem.isEmpty() && (this.cost.getRawValue() > 0 || this.isFree);
	}
	
	public boolean hasStock(IInventory storage)
	{
		if(this.sellItem.isEmpty())
			return false;
		return stockCount(storage) > 0;
	}
	
	public int stockCount(IInventory storage)
	{
		if(this.sellItem.isEmpty())
			return 0;
		if(this.restriction == ItemTradeRestrictions.TICKET)
		{
			return InventoryUtil.GetItemTagCount(storage, TicketItem.TICKET_MATERIAL_TAG) / this.sellItem.getCount();
		}
		else
		{
			return InventoryUtil.GetItemCount(storage, this.sellItem) / this.sellItem.getCount();
		}
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
		sellItem.write(tradeNBT);
		tradeNBT.putString("TradeDirection", this.tradeDirection.name());
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
		
		sellItem = ItemStack.read(nbt);
		
		
		//Set the Trade Direction
		if(nbt.contains("TradeDirection", Constants.NBT.TAG_STRING))
			this.tradeDirection = loadTradeDirection(nbt.getString("TradeDirection"));
		else
			this.tradeDirection = ItemTradeType.SALE;
		
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
	
	public static ItemTradeType loadTradeDirection(String name)
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
	
	public int tradesPossibleInStorage(IInventory inventory)
	{
		int itemCount = 0;
		for(int i = 0; i < inventory.getSizeInventory(); i++)
		{
			ItemStack itemStack = inventory.getStackInSlot(i);
			if(itemStack.getItem() == sellItem.getItem())
			{
				itemCount += itemStack.getCount();
			}
		}
		return itemCount / sellItem.getCount();
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
