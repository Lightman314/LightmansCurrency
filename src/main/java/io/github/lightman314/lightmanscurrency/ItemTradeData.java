package io.github.lightman314.lightmanscurrency;


import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
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
	
	public ItemStack getDisplayItem()
	{
		if(this.customName == "")
			return getSellItem();
		return getSellItem().setHoverName(new TextComponent("§6" + this.customName));
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
	
	public static EquipmentSlot getSlotFromRestriction(TradeRestrictions restriction)
	{
		if(restriction == TradeRestrictions.ARMOR_HEAD)
			return EquipmentSlot.HEAD;
		if(restriction == TradeRestrictions.ARMOR_CHEST)
			return EquipmentSlot.CHEST;
		if(restriction == TradeRestrictions.ARMOR_LEGS)
			return EquipmentSlot.LEGS;
		if(restriction == TradeRestrictions.ARMOR_FEET)
			return EquipmentSlot.FEET;
		
		return null;
	}
	
	public boolean isValid()
	{
		return !this.sellItem.isEmpty() && (this.cost.getRawValue() > 0 || this.isFree);
	}
	
	
	public boolean hasStock(Container storage)
	{
		if(this.sellItem.isEmpty())
			return false;
		return stockCount(storage) > 0;
	}
	
	public int stockCount(Container storage)
	{
		if(this.sellItem.isEmpty())
			return 0;
		int itemCount = 0;
		for(int i = 0; i < storage.getContainerSize(); i++)
		{
			ItemStack itemStack = storage.getItem(i);
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
	
	public CompoundTag getAsNBT()
	{
		CompoundTag tradeNBT = new CompoundTag();
		sellItem.save(tradeNBT);
		this.cost.writeToNBT(tradeNBT,"Price");
		tradeNBT.putString("TradeDirection", this.tradeDirection.name());
		
		tradeNBT.putString("Restrictions", this.restriction.name());
		tradeNBT.putBoolean("IsFree", this.isFree);
		tradeNBT.putString("CustomName", this.customName);
		return tradeNBT;
	}
	
	public static CompoundTag saveAllData(CompoundTag nbt, NonNullList<ItemTradeData> data)
	{
		return saveAllData(nbt, data, "Trades");
	}
	
	public static CompoundTag saveAllData(CompoundTag nbt, NonNullList<ItemTradeData> data, String key)
	{
		ListTag listNBT = new ListTag();
		
		for(int i = 0; i < data.size(); i++)
		{
			listNBT.add(data.get(i).getAsNBT());
		}
		
		if(listNBT.size() > 0)
			nbt.put(key, listNBT);
		
		return nbt;
	}
	
	public static NonNullList<ItemTradeData> loadAllData(CompoundTag nbt, int arraySize)
	{
		return loadAllData(DEFAULT_KEY, nbt, arraySize);
	}
	
	public static NonNullList<ItemTradeData> loadAllData(String key, CompoundTag nbt, int arraySize)
	{
		ListTag listNBT = nbt.getList(key, Constants.NBT.TAG_COMPOUND);
		
		NonNullList<ItemTradeData> data = listOfSize(arraySize);
		
		for(int i = 0; i < listNBT.size() && i < arraySize; i++)
		{
			//CompoundNBT compoundNBT = listNBT.getCompound(i);
			data.get(i).loadFromNBT(listNBT.getCompound(i));
		}
		
		return data;
	}
	
	public void loadFromNBT(CompoundTag nbt)
	{
		sellItem = ItemStack.of(nbt);
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
	
	public int tradesPossibleInStorage(Container inventory)
	{
		int itemCount = 0;
		for(int i = 0; i < inventory.getContainerSize(); i++)
		{
			ItemStack itemStack = inventory.getItem(i);
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
