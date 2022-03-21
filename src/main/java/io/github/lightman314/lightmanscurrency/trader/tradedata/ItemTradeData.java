package io.github.lightman314.lightmanscurrency.trader.tradedata;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData.TradeComparisonResult.ProductComparisonResult;
import io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ItemTradeData extends TradeData {
	
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
	
	ItemTradeRestriction restriction = ItemTradeRestriction.NONE;
	ItemStack sellItem = ItemStack.EMPTY;
	ItemStack barterItem = ItemStack.EMPTY;
	ItemTradeType tradeType = ItemTradeType.SALE;
	String customName = "";
	
	public ItemStack getSellItem()
	{
		return this.restriction.modifySellItem(this.sellItem.copy(), this);
	}
	
	public ItemStack getBarterItem()
	{
		return this.barterItem.copy();
	}
	
	public void setSellItem(ItemStack itemStack)
	{
		if(this.restriction.allowSellItem(itemStack) || itemStack.isEmpty())
			this.sellItem = this.restriction.filterSellItem(itemStack).copy();
	}
	
	public void setBarterItem(ItemStack itemStack)
	{
		this.barterItem = itemStack.copy();
	}
	
	public boolean hasCustomName() { return !this.customName.isEmpty(); }
	
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
	
	public ItemTradeRestriction getRestriction()
	{
		return this.restriction;
	}
	
	public void setRestriction(ItemTradeRestriction restriction)
	{
		this.restriction = restriction;
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
			if(this.cost.isFree())
				return 1;
			if(this.cost.getRawValue() == 0)
				return 0;
			long coinValue = trader.getStoredMoney().getRawValue();
			long price = this.cost.getRawValue();
			return (int)(coinValue / price);
		}
		else if(this.tradeType == ItemTradeType.SALE || this.tradeType == ItemTradeType.BARTER)
		{
			return this.restriction.getSaleStock(this.sellItem, trader.getStorage());
		}
		else //Other types are not handled yet.
			return 0;
	}
	
	public void RemoveItemsFromStorage(Container storage)
	{
		this.restriction.removeItemsFromStorage(this.sellItem, storage);
	}
	
	@Override
	public CompoundTag getAsNBT() {
		CompoundTag tradeNBT = super.getAsNBT();
		CompoundTag sellItemCompound = new CompoundTag();
		sellItem.save(sellItemCompound);
		tradeNBT.put("SellItem", sellItemCompound);
		CompoundTag barterItemCompound = new CompoundTag();
		barterItem.save(barterItemCompound);
		tradeNBT.put("BarterItem", barterItemCompound);
		tradeNBT.putString("TradeDirection", this.tradeType.name());
		tradeNBT.putString("Restrictions", this.restriction.getRegistryName().toString());
		tradeNBT.putString("CustomName", this.customName);
		return tradeNBT;
	}
	
	public static CompoundTag saveAllData(CompoundTag nbt, List<ItemTradeData> data)
	{
		return saveAllData(nbt, data, DEFAULT_KEY);
	}
	
	public static CompoundTag saveAllData(CompoundTag nbt, List<ItemTradeData> data, String key)
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
	
	public static ItemTradeData loadData(CompoundTag nbt) {
		ItemTradeData trade = new ItemTradeData();
		trade.loadFromNBT(nbt);
		return trade;
	}
	
	public static List<ItemTradeData> loadAllData(CompoundTag nbt, int arraySize)
	{
		return loadAllData(DEFAULT_KEY, nbt, arraySize);
	}
	
	public static List<ItemTradeData> loadAllData(String key, CompoundTag nbt, int arraySize)
	{
		ListTag listNBT = nbt.getList(key, Tag.TAG_COMPOUND);
		
		List<ItemTradeData> data = listOfSize(arraySize);
		
		for(int i = 0; i < listNBT.size() && i < arraySize; i++)
		{
			//CompoundNBT compoundNBT = listNBT.getCompound(i);
			data.get(i).loadFromNBT(listNBT.getCompound(i));
		}
		
		return data;
	}
	
	@Override
	public void loadFromNBT(CompoundTag nbt)
	{
		
		super.loadFromNBT(nbt);
		
		//Load the Sell Item
		if(nbt.contains("SellItem", Tag.TAG_COMPOUND))
			sellItem = ItemStack.of(nbt.getCompound("SellItem"));
		else //Load old format from before the bartering system was made
			sellItem = ItemStack.of(nbt);
		
		//Load the Barter Item
		if(nbt.contains("BarterItem", Tag.TAG_COMPOUND))
			barterItem = ItemStack.of(nbt.getCompound("BarterItem"));
		else
			barterItem = ItemStack.EMPTY;
		
		//Set the Trade Direction
		if(nbt.contains("TradeDirection", Tag.TAG_STRING))
			this.tradeType = loadTradeType(nbt.getString("TradeDirection"));
		else
			this.tradeType = ItemTradeType.SALE;
		
		//Set the restrictions
		if(nbt.contains("Restrictions"))
			this.restriction = ItemTradeRestriction.get(nbt.getString("Restrictions"));
		else
			this.restriction = ItemTradeRestriction.NONE;
		
		if(nbt.contains("CustomName"))
			this.customName = nbt.getString("CustomName");
		else
			this.customName = "";
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
	
	public static List<ItemTradeData> listOfSize(int tradeCount)
	{
		List<ItemTradeData> data = Lists.newArrayList();
		while(data.size() < tradeCount)
			data.add(new ItemTradeData());
		return data;
	}
	
	public void markRulesDirty() { }
	
	public TradeComparisonResult compare(TradeData otherTrade) {
		TradeComparisonResult result = new TradeComparisonResult();
		if(otherTrade instanceof ItemTradeData)
		{
			ItemTradeData otherItemTrade = (ItemTradeData)otherTrade;
			//Compare sell items
			result.addProductResult(ProductComparisonResult.CompareItem(this.sellItem, otherItemTrade.sellItem));
			//Compare barter items
			if(this.isBarter())
				result.addProductResult(ProductComparisonResult.CompareItem(this.barterItem, otherItemTrade.barterItem));
			//Compare prices
			if(!this.isBarter())
				result.setPriceResult(this.getCost().getRawValue() - otherTrade.getCost().getRawValue());
			//Compare types
			result.setTypeResult(this.tradeType == otherItemTrade.tradeType);
		}
		//Return the comparison results
		return result;
	}
	
	public boolean AcceptableDifferences(TradeComparisonResult result) {
		
		//Confirm the types match
		if(!result.TypeMatches() || !result.isCompatible())
			return false;
		
		//Confirm the sell item is acceptable
		if(result.getProductResultCount() < 1)
			return false;
		ProductComparisonResult sellResult = result.getProductResult(0);
		if(sellResult.SameProductType() && sellResult.SameProductNBT())
		{
			if(this.isSale() || this.isBarter())
			{
				//Sell product should be greater than or equal to pass
				if(sellResult.ProductQuantityDifference() > 0)
					return false;
			}
			else if(this.isPurchase())
			{
				//Purchase product should be less than or equal to pass
				if(sellResult.ProductQuantityDifference() < 0)
					return false;
			}
		}
		else //Item & tag don't match. Failure.
			return false;
		//Confirm the barter item is acceptable
		if(this.isBarter())
		{
			if(result.getProductResultCount() < 2)
				return false;
			ProductComparisonResult barterResult = result.getProductResult(1);
			if(barterResult.SameProductType() && barterResult.SameProductNBT())
			{
				//Barter product should be less than or equal to pass
				if(barterResult.ProductQuantityDifference() < 0)
					return false;
			}
			else //Item & tag don't match. Failure.
				return false;
		}
		//Product is acceptable, now check the price
		if(this.isSale() && result.isPriceExpensive())
			return false;
		if(this.isPurchase() && result.isPriceCheaper())
			return false;
		
		//Products, price, and types are all acceptable.
		return true;
	}
	
}
