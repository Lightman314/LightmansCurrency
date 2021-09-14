package io.github.lightman314.lightmanscurrency;


import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
//import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

public class ItemTradeData {
	
	public enum TradeRestrictions { NONE, ARMOR_HEAD, ARMOR_CHEST, ARMOR_LEGS, ARMOR_FEET }
	public enum TradeDirection { SALE, PURCHASE }
	
	public static int MaxTradeDirectionStringLength()
	{ 
		int length = 0;
		for(TradeDirection value : TradeDirection.values())
		{
			int thisLength = value.name().length();
			if(thisLength > length)
				length = thisLength;
		}
		return length;
	}
	
	public static final String DEFAULT_KEY = "Trades";
	public static final int MAX_CUSTOMNAME_LENGTH = 30;
	
	ItemStack sellItem = ItemStack.EMPTY;
	CoinValue cost = new CoinValue();
	TradeDirection tradeDirection = TradeDirection.SALE;
	boolean isFree = false;
	String customName = "";
	TradeRestrictions restriction = TradeRestrictions.NONE;
	
	public ItemTradeData()
	{
		clear();
	}
	
	public ItemStack getSellItem()
	{
		return this.sellItem.copy();
	}
	
	public ItemStack getDisplayItem(IInventory storage, boolean isCreative)
	{
		ItemStack displayItem = getSellItem();
		//Get the item tag if there is one, if not just create a blank one
		CompoundNBT itemTag = displayItem.getOrCreateTag();
		itemTag.putBoolean("LC_DisplayItem", true);
		//Always to custom name data first, as it will re-load the item tag after setting the name.
		if(this.customName != "")
			itemTag.putString("LC_CustomName", this.customName);
		//Get amount in stock
		itemTag.putInt("LC_StockAmount", isCreative ? -1 : this.stockCount(storage));
		displayItem.setTag(itemTag);
		return displayItem;
	}
	
	public void setSellItem(ItemStack itemStack)
	{
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
	
	public boolean isFree()
	{
		return this.isFree && cost.getRawValue() <= 0;
	}
	
	public void setFree(boolean isFree)
	{
		this.isFree = isFree;
		LightmansCurrency.LogInfo("Set free state of a trade to " + isFree);
	}
	
	public CoinValue getCost()
	{
		return this.cost;
	}
	
	public void setCost(CoinValue value)
	{
		this.cost = value;
	}
	
	public TradeDirection getTradeDirection()
	{
		return this.tradeDirection;
	}
	
	public void setTradeDirection(TradeDirection tradeDirection)
	{
		this.tradeDirection = tradeDirection;
	}
	
	public TradeRestrictions getRestriction()
	{
		return this.restriction;
	}
	
	public void setRestriction(TradeRestrictions restriction)
	{
		this.restriction = restriction;
	}
	
	public static EquipmentSlotType getSlotFromRestriction(TradeRestrictions restriction)
	{
		if(restriction == TradeRestrictions.ARMOR_HEAD)
			return EquipmentSlotType.HEAD;
		if(restriction == TradeRestrictions.ARMOR_CHEST)
			return EquipmentSlotType.CHEST;
		if(restriction == TradeRestrictions.ARMOR_LEGS)
			return EquipmentSlotType.LEGS;
		if(restriction == TradeRestrictions.ARMOR_FEET)
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
		int itemCount = 0;
		for(int i = 0; i < storage.getSizeInventory(); i++)
		{
			ItemStack itemStack = storage.getStackInSlot(i);
			if(itemStack.getItem() == sellItem.getItem())
			{
				if(itemStack.hasTag() == sellItem.hasTag())
				{
					if(itemStack.hasTag())
					{
						if(itemStack.getTag().equals(sellItem.getTag()))
						{
							itemCount += itemStack.getCount();
						}
					}
					else
					{
						itemCount += itemStack.getCount();
					}
				}
			}
		}
		return itemCount / sellItem.getCount();
	}
	
	public CompoundNBT getAsNBT() {
		CompoundNBT tradeNBT = new CompoundNBT();
		sellItem.write(tradeNBT);
		this.cost.writeToNBT(tradeNBT,"Price");
		tradeNBT.putString("TradeDirection", this.tradeDirection.name());
		
		tradeNBT.putString("Restrictions", this.restriction.name());
		tradeNBT.putBoolean("IsFree", this.isFree);
		tradeNBT.putString("CustomName", this.customName);
		return tradeNBT;
	}
	
	public static CompoundNBT saveAllData(CompoundNBT nbt, NonNullList<ItemTradeData> data)
	{
		return saveAllData(nbt, data, "Trades");
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
	
	public void loadFromNBT(CompoundNBT nbt)
	{
		sellItem = ItemStack.read(nbt);
		if(nbt.contains("Price", Constants.NBT.TAG_INT))
			cost.readFromOldValue(nbt.getInt("Price"));
		else if(nbt.contains("Price", Constants.NBT.TAG_LIST))
			cost.readFromNBT(nbt, "Price");
		
		//Set the Trade Direction
		if(nbt.contains("TradeDirection", Constants.NBT.TAG_STRING))
			this.tradeDirection = loadTradeDirection(nbt.getString("TradeDirection"));
		else
			this.tradeDirection = TradeDirection.SALE;
		
		//Set the restrictions
		if(nbt.contains("Restrictions"))
			this.restriction = loadRestriction(nbt.getString("Restrictions"));
		else
			this.restriction = TradeRestrictions.NONE;
		//Set whether it's free or not
		if(nbt.contains("IsFree"))
			this.isFree = nbt.getBoolean("IsFree");
		else
			this.isFree = false;
		
		if(nbt.contains("CustomName"))
			this.customName = nbt.getString("CustomName");
		else
			this.customName = "";
	}
	
	private static TradeRestrictions loadRestriction(String name)
	{
		TradeRestrictions value = TradeRestrictions.NONE;
		try {
			value = TradeRestrictions.valueOf(name);
		}
		catch (IllegalArgumentException exception)
		{
			LightmansCurrency.LogError("Could not load '" + name + "' as a TradeRestriction.");
		}
		return value;
	}
	
	public static TradeDirection loadTradeDirection(String name)
	{
		TradeDirection value = TradeDirection.SALE;
		try {
			value = TradeDirection.valueOf(name);
		}
		catch (IllegalArgumentException exception)
		{
			LightmansCurrency.LogError("Could not load '" + name + "' as a TradeDirection.");
		}
		return value;
	}
	
	public boolean hasEnoughMoney(CoinValue coinStorage)
	{
		return tradesPossibleWithStoredMoney(coinStorage) > 0;
	}
	
	public long tradesPossibleWithStoredMoney(CoinValue coinStorage)
	{
		if(this.isFree)
			return 1;
		if(this.cost.getRawValue() == 0) //To avoid dividing by 0
			return 0;
		long coinValue = coinStorage.getRawValue();
		long price = this.cost.getRawValue();
		return coinValue / price;
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
	
	public void clear()
	{
		sellItem = ItemStack.EMPTY;
		cost = new CoinValue();
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
	
}
