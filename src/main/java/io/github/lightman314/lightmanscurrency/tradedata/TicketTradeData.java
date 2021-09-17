package io.github.lightman314.lightmanscurrency.tradedata;


import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;

public class TicketTradeData extends TradeData {
	
	public enum TicketTradeType { SALE, BARTER }
	
	public static int MaxTradeDirectionStringLength()
	{ 
		int length = 0;
		for(TicketTradeType value : TicketTradeType.values())
		{
			int thisLength = value.name().length();
			if(thisLength > length)
				length = thisLength;
		}
		return length;
	}
	
	public static final int MAX_CUSTOMNAME_LENGTH = 30;
	
	UUID ticketID = null;
	TicketTradeType tradeDirection = TicketTradeType.SALE;
	String customName = "";
	
	public ItemTradeData emulateItemTrade()
	{
		
		ItemTradeData fakeTrade = new ItemTradeData();
		fakeTrade.cost = this.cost;
		fakeTrade.sellItem = this.getTicket();
		fakeTrade.rules = this.rules;
		return fakeTrade;
		
	}
	
	public UUID getTicketID()
	{
		return this.ticketID;
	}
	
	public ItemStack getTicket()
	{
		if(this.ticketID != null)
		{
			ItemStack ticket = TicketItem.CreateTicket(this.ticketID, 1);
			if(this.customName != "")
				ticket.setDisplayName(new StringTextComponent(this.customName));
			return ticket;
		}
		return ItemStack.EMPTY;
	}
	
	public ItemStack getDisplayItem(IInventory storage, boolean isCreative, CoinValue storedMoney)
	{
		ItemStack displayItem = getTicket();
		if(displayItem.isEmpty())
			return displayItem;
		//Get the item tag
		CompoundNBT itemTag = displayItem.getOrCreateTag();
		if(this.tradeDirection == TicketTradeType.SALE)
		{
			itemTag.putBoolean("LC_DisplayItem", true);
			itemTag.putInt("LC_StockAmount", isCreative ? -1 : this.stockCount(storage));
		}
		displayItem.setTag(itemTag);
		return displayItem;
	}
	
	public void setTicketID(UUID ticketID)
	{
		this.ticketID = ticketID;
	}
	
	public String getCustomName()
	{
		return this.customName;
	}
	
	public void setCustomName(String customName)
	{
		this.customName = customName;
	}
	
	public TicketTradeType getTradeDirection()
	{
		return this.tradeDirection;
	}
	
	public void setTradeDirection(TicketTradeType tradeDirection)
	{
		this.tradeDirection = tradeDirection;
	}
	
	public boolean isValid()
	{
		return this.ticketID != null && (this.cost.getRawValue() > 0 || this.isFree);
	}
	
	public boolean hasStock(IInventory storage)
	{
		if(this.ticketID == null)
			return false;
		return stockCount(storage) > 0;
	}
	
	public int stockCount(IInventory storage)
	{
		int count = 0;
		for(int i = 0; i < storage.getSizeInventory(); i++)
		{
			ItemStack thisItem = storage.getStackInSlot(i);
			if(thisItem.getItem().getTags().contains(new ResourceLocation(LightmansCurrency.MODID,"ticket_material")))
				count += thisItem.getCount();
		}
		return count;
	}
	
	@Override
	public CompoundNBT getAsNBT() {
		CompoundNBT tradeNBT = super.getAsNBT();
		if(this.ticketID != null)
			tradeNBT.putUniqueId("TicketID", this.ticketID);
		tradeNBT.putString("TradeDirection", this.tradeDirection.name());
		tradeNBT.putString("CustomName", this.customName);
		return tradeNBT;
	}
	
	public static CompoundNBT saveAllData(CompoundNBT nbt, NonNullList<TicketTradeData> data)
	{
		return saveAllData(nbt, data, DEFAULT_KEY);
	}
	
	public static CompoundNBT saveAllData(CompoundNBT nbt, NonNullList<TicketTradeData> data, String key)
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
	
	public static NonNullList<TicketTradeData> loadAllData(CompoundNBT nbt, int arraySize)
	{
		return loadAllData(DEFAULT_KEY, nbt, arraySize);
	}
	
	public static NonNullList<TicketTradeData> loadAllData(String key, CompoundNBT nbt, int arraySize)
	{
		ListNBT listNBT = nbt.getList(key, Constants.NBT.TAG_COMPOUND);
		
		NonNullList<TicketTradeData> data = listOfSize(arraySize);
		
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
		
		this.ticketID = null;
		if(nbt.contains("TicketID"))
			this.ticketID = nbt.getUniqueId("TicketID");
		
		//Set the Trade Direction
		if(nbt.contains("TradeDirection", Constants.NBT.TAG_STRING))
			this.tradeDirection = loadTradeDirection(nbt.getString("TradeDirection"));
		else
			this.tradeDirection = TicketTradeType.SALE;
		
		if(nbt.contains("CustomName"))
			this.customName = nbt.getString("CustomName");
		else
			this.customName = "";
	}
	
	public static TicketTradeType loadTradeDirection(String name)
	{
		TicketTradeType value = TicketTradeType.SALE;
		try {
			value = TicketTradeType.valueOf(name);
		}
		catch (IllegalArgumentException exception)
		{
			LightmansCurrency.LogError("Could not load '" + name + "' as a TradeDirection.");
		}
		return value;
	}
	
	public static NonNullList<TicketTradeData> listOfSize(int tradeCount)
	{
		NonNullList<TicketTradeData> data = NonNullList.withSize(tradeCount, new TicketTradeData());
		for(int i = 0; i < tradeCount; i++)
		{
			data.set(i, new TicketTradeData());
		}
		return data;
	}
	
}
