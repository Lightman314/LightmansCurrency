package io.github.lightman314.lightmanscurrency.common.money;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

public class CoinValue
{
	
	public enum ValueType { DEFAULT, VALUE }
	
	public static final String DEFAULT_KEY = "CoinValue";
	
	private boolean isFree = false;
	public boolean isFree() { return this.isFree; }
	public void setFree(boolean free) { this.isFree = free; if(this.isFree) this.coinValues.clear(); }
	public final List<CoinValuePair> coinValues;
	
	public boolean hasAny() { return this.coinValues.size() > 0; }
	
	public boolean isValid() { return this.isFree || this.coinValues.size() > 0; }
	
	public CoinValue(CompoundNBT compound)
	{
		this.coinValues = new ArrayList<>();
		this.load(compound, DEFAULT_KEY);
		this.roundValue();
	}
	
	public CoinValue(long rawValue)
	{
		this.coinValues = new ArrayList<>();
		this.loadFromOldValue(rawValue);
		this.roundValue();
	}
	
	public CoinValue(NonNullList<ItemStack> inventory)
	{
		this.coinValues = new ArrayList<>();
		this.loadFromOldValue(MoneyUtil.getValue(inventory));
		this.roundValue();
	}
	
	public CoinValue(CoinValue otherValue)
	{
		this.isFree = otherValue.isFree;
		this.coinValues = new ArrayList<>();
		if(!this.isFree)
		{
			for(CoinValuePair pricePair : otherValue.coinValues)
    		{
    			this.coinValues.add(pricePair.copy());
    		}
			this.roundValue();
		}
	}
	
	@SafeVarargs
	public CoinValue(CoinValuePair... priceValues)
	{
		this.coinValues = new ArrayList<>();
		for(CoinValuePair value : priceValues)
		{
			for (CoinValuePair coinValue : this.coinValues) {
				if (coinValue.coin == value.coin) {
					coinValue.amount += value.amount;
					value.amount = 0;
				}
			}
			if(value.amount > 0)
			{
				this.coinValues.add(value);
			}
		}
		this.roundValue();
	}
	
	//Private constructors for easyBuilds
	private CoinValue(List<CoinValuePair> priceValues, boolean validateChain)
	{
		if(validateChain)
		{
			this.coinValues = new ArrayList<>();
			for (CoinValuePair priceValue : priceValues) this.addValue(priceValue.coin, priceValue.amount);
		}
		else
		{
			this.coinValues = priceValues;
			this.roundValue();
		}
	}
	
	public CompoundNBT save(CompoundNBT compound, String key)
	{
		if(this.isFree)
		{
			compound.putBoolean(key, true);
		}
		else
		{
			ListNBT list = new ListNBT();
    		for(CoinValuePair value : coinValues)
    		{
				CompoundNBT thisCompound = new CompoundNBT();
    			//new ItemStack(null).write(nbt)
				ResourceLocation resource = ForgeRegistries.ITEMS.getKey(value.coin);
    			if(resource != null && MoneyUtil.isCoin(value.coin))
    			{
    				thisCompound.putString("id", resource.toString());
    				thisCompound.putInt("amount", value.amount);
    				list.add(thisCompound);
    			}
    		}
			compound.put(key, list);
		}
		
		return compound;
	}
	
	public void load(CompoundNBT compound, String key)
	{
		if(compound.contains(key, Constants.NBT.TAG_INT))
		{
			//Read old value
			this.loadFromOldValue(compound.getInt(key));
		}
		else if(compound.contains(key, Constants.NBT.TAG_LIST))
		{
			//Read full value
			ListNBT listNBT = compound.getList(key, Constants.NBT.TAG_COMPOUND);
			this.coinValues.clear();
			for(int i = 0; i < listNBT.size(); i++)
			{
				CompoundNBT thisCompound = listNBT.getCompound(i);
				Item priceCoin = ForgeRegistries.ITEMS.getValue(new ResourceLocation(thisCompound.getString("id")));
				int amount = thisCompound.getInt("amount");
				this.coinValues.add(new CoinValuePair(priceCoin,amount));
			}
		}
		else if(compound.contains(key))
		{
			//Read free state
			this.setFree(compound.getBoolean(key));
		}
		
	}

	public static CoinValue from(CompoundNBT compound, String key) {
		CoinValue val = new CoinValue();
		val.load(compound, key);
		return val;
	}
	
	public void loadFromOldValue(long oldPrice)
	{
		this.coinValues.clear();
		List<ItemStack> coinItems = MoneyUtil.getCoinsOfValue(oldPrice);
		for(ItemStack stack : coinItems)
		{
			Item coinItem = stack.getItem();
			int amount = stack.getCount();
			for (CoinValuePair coinValue : this.coinValues) {
				if (coinValue.coin == coinItem) {
					coinValue.amount += amount;
					amount = 0;
				}
			}
			if(amount > 0)
			{
				this.coinValues.add(new CoinValuePair(coinItem,amount));
			}
		}
		this.sortValue();
	}

	public final void encode(PacketBuffer buffer) { buffer.writeNbt(this.save(new CompoundNBT(), DEFAULT_KEY)); }

	public static CoinValue decode(PacketBuffer buffer) { return from(buffer.readAnySizeNbt(), DEFAULT_KEY); }
	
	public void addValue(CoinValue other)
	{
		CoinValue otherPrice = other.copy();
		for(int i = 0; i < this.coinValues.size(); i++)
		{
			for(int j = 0; j < otherPrice.coinValues.size(); j++)
			{
				if(this.coinValues.get(i).coin == otherPrice.coinValues.get(j).coin)
				{
					this.coinValues.get(i).amount += otherPrice.coinValues.get(j).amount;
					otherPrice.coinValues.get(j).amount = 0;
				}
			}
		}
		for(CoinValuePair pair : otherPrice.coinValues)
		{
			if(pair.amount > 0)
			{
				this.coinValues.add(pair);
			}
		}
		this.roundValue();
	}
	
	public void addValue(Item coin, int amount)
	{
		long newValue = this.getRawValue() + (MoneyUtil.getValue(coin) * amount);
		this.loadFromOldValue(newValue);
		this.roundValue();
	}
	
	public void removeValue(Item coin, int amount)
	{
		long newValue = this.getRawValue() - (MoneyUtil.getValue(coin) * amount);
		this.loadFromOldValue(newValue);
		this.roundValue();
	}
	
	public void removeValue(CoinValue otherValue) {
		long thisValue = this.getRawValue();
		long otherVal = otherValue.getRawValue();
		if(otherVal > thisValue)
			throw new RuntimeException("Other Coin Value is greater than this value.");
		this.loadFromOldValue(thisValue - otherVal);
	}
	
	private void roundValue()
	{
		while(needsRounding())
		{
			for(int i = 0; i < this.coinValues.size(); i++)
			{
				if(needsRounding(i))
				{
					CoinValuePair pair = this.coinValues.get(i);
					Pair<Item,Integer> conversion = MoneyUtil.getUpwardConversion(pair.coin);
					int largeAmount = 0;
					while(pair.amount >= conversion.getSecond())
					{
						largeAmount++;
						pair.amount -= conversion.getSecond();
					}
					//If there's none of this coin left, remove if from the list
					if(pair.amount == 0)
					{
						this.coinValues.remove(i);
						//Shrink the index to avoid oversight
						i--;
					}
					//Add the larger amount to the price values list
					for(CoinValuePair thisPair : this.coinValues)
					{
						if(thisPair.coin == conversion.getFirst())
						{
							thisPair.amount += largeAmount;
							largeAmount = 0;
						}
					}
					if(largeAmount > 0)
					{
						this.coinValues.add(new CoinValuePair(conversion.getFirst(), largeAmount));
					}
				}
			}
		}
		this.sortValue();
	}
	
	private void sortValue()
	{
		List<CoinValuePair> newList = new ArrayList<>();
		while(this.coinValues.size() > 0)
		{
			//Get the largest index
			long largestValue = MoneyUtil.getValue(this.coinValues.get(0).coin);
			int largestIndex = 0;
			for(int i = 1; i < this.coinValues.size(); i++)
			{
				long thisValue = MoneyUtil.getValue(this.coinValues.get(i).coin);
				if(thisValue > largestValue)
				{
					largestIndex = i;
					largestValue = thisValue;
				}
			}
			newList.add(this.coinValues.get(largestIndex));
			this.coinValues.remove(largestIndex);
		}
		this.coinValues.addAll(newList);
	}
	
	private boolean needsRounding()
	{
		for(int i = 0; i < this.coinValues.size(); i++)
		{
			if(this.needsRounding(i))
				return true;
		}
		return false;
	}
	
	private boolean needsRounding(int index)
	{
		CoinValuePair pair = this.coinValues.get(index);
		Pair<Item,Integer> conversion = MoneyUtil.getUpwardConversion(pair.coin);
		if(conversion != null)
		{
			return pair.amount >= conversion.getSecond();
		}
		return false;
	}
	
	public List<CoinValuePair> getEntries() { return this.coinValues; }

	public List<ItemStack> getAsItemList() {
		List<ItemStack> items = new ArrayList<>();
		for(CoinValuePair entry : this.coinValues)
			items.add(new ItemStack(entry.coin, entry.amount));
		return items;
	}

	public int getEntry(Item coinItem)
	{
		for(CoinValuePair pair : this.coinValues)
		{
			if(pair.coin == coinItem)
				return pair.amount;
		}
		return 0;
	}
	
	public CoinValue copy()
	{
		return new CoinValue(this);
	}
	
	public double getDisplayValue()
	{
		double totalValue = 0d;
		for (CoinValuePair pricePair : this.coinValues) {
			CoinData coinData = MoneyUtil.getData(pricePair.coin);
			if (coinData != null) {
				totalValue += coinData.getDisplayValue() * pricePair.amount;
			}
		}
		return totalValue;
	}
	
	public String getString() { return this.getString(""); }
	
	public String getString(String emptyFiller)
	{
		
		if(this.isFree)
			return EasyText.translatable("gui.coinvalue.free").getString();
		
		switch(Config.SERVER.coinValueType.get())
		{
		case DEFAULT:
			StringBuilder string = new StringBuilder();
			for (CoinValuePair pricePair : this.coinValues) {
				CoinData coinData = MoneyUtil.getData(pricePair.coin);
				if (coinData != null) {
					string.append(pricePair.amount);
					string.append(coinData.getInitial().getString());
				}
			}
        	if(string.toString().isEmpty())
        		return emptyFiller;
        	return string.toString();
		case VALUE:
        	return Config.formatValueDisplay(this.getDisplayValue());
        	default:
        		return "?";
		}
		
	}
	
	public IFormattableTextComponent getComponent() { return this.getComponent(""); }
	
	public IFormattableTextComponent getComponent(String emptyFiller) { return EasyText.literal(this.getString(emptyFiller)); }
	
	public long getRawValue()
	{
		long value = 0;
		for(CoinValuePair pricePair : this.coinValues)
		{
			CoinData coinData = MoneyUtil.getData(pricePair.coin);
			if(coinData != null)
			{
				value += pricePair.amount * coinData.getValue();
			}
		}
		//LightmansCurrency.LOGGER.info("Accumulated Value: " + value + " PricePair count: " + this.priceValues.size());
		return value;
	}
	
	public CoinValue ApplyMultiplier(double costMultiplier)
	{
		CoinValue multipliedValue = new CoinValue();
		if(this.isFree)
		{
			//Anything multiplied by free is still free
			multipliedValue.setFree(true);
			return multipliedValue;
		}
		costMultiplier = MathUtil.clamp(costMultiplier, 0d, 10d);

		for (CoinValuePair coinValue : this.coinValues) {
			int amount = coinValue.amount;
			Item coin = coinValue.coin;
			double newAmount = amount * costMultiplier;
			double leftoverAmount = newAmount % 1d;
			multipliedValue.addValue(coin, (int) newAmount);
			CoinData coinData = MoneyUtil.getData(coin);
			while (coinData != null && coinData.convertsDownwards() && leftoverAmount > 0d) {
				Pair<Item, Integer> conversion = coinData.getDownwardConversion();
				coin = conversion.getFirst();
				coinData = MoneyUtil.getData(coin);
				newAmount = leftoverAmount * conversion.getSecond();
				leftoverAmount = newAmount % 1d;
				multipliedValue.addValue(coin, (int) newAmount);
			}
		}
		if(multipliedValue.getRawValue() <= 0) //If it became free, flag the result as free.
			multipliedValue.setFree(true);
		return multipliedValue;
	}
	
	/**
	 * Gets the two most significant coin stacks from the given value for use as a villager price
	 */
	public Pair<ItemStack,ItemStack> getTradeItems()
	{
		List<ItemStack> coins = MoneyUtil.getCoinsOfValue(this);
		ItemStack stack1 = ItemStack.EMPTY;
		ItemStack stack2 = ItemStack.EMPTY;
		//Get the first stack
		if(coins.size() > 0)
			stack1 = coins.get(0);
		else
			LightmansCurrency.LogWarning("A CoinValue used in a trade gave no coins as an output.");
		//Get the second stack
		if(coins.size() > 1)
			stack2 = coins.get(1);
		//Warn about any excess stacks
		if(coins.size() > 2)
			LightmansCurrency.LogWarning("A CoinValue used in a trade gave more than two stacks of coins of output.");
		return new Pair<>(stack1, stack2);
	}
	
	/**
	 * Gets the most significant coin stack from the given value for use as a villager buy item
	 */
	public ItemStack getTradeItem()
	{
		List<ItemStack> coins = MoneyUtil.getCoinsOfValue(this);
		ItemStack stack = ItemStack.EMPTY;
		//Get the first stack
		if(coins.size() > 0)
			stack = coins.get(0);
		else
			LightmansCurrency.LogWarning("A CoinValue used in a trade gave no coins as an output.");
		//Warn about any excess stacks
		if(coins.size() > 1)
			LightmansCurrency.LogWarning("A CoinValue used in a trade output gave more than one stack of coins of output.");
		return stack;
	}
	
	public static class CoinValuePair
	{
		
		public final Item coin;
		public int amount;
		
		public CoinValuePair(Item coin, int amount)
		{
			this.coin = coin;
			this.amount = amount;
		}
		
		public CoinValuePair copy()
		{
			return new CoinValuePair(this.coin,this.amount);
		}
		
	}
	
	public static final CoinValue EMPTY = new CoinValue();
	
	public static CoinValue easyBuild1(ItemStack... stack)
	{
		List<CoinValuePair> pairs = new ArrayList<>();
		for(int i = 0; i < stack.length; i++)
		{
			if(MoneyUtil.isCoin(stack[i]))
				pairs.add(new CoinValuePair(stack[i].getItem(), stack[i].getCount()));
			else
				LightmansCurrency.LogWarning("CoinValue.easyBuild1: ItemStack at index " + i + " is not a valid coin.");
		}
		return new CoinValue(pairs, false);
	}
	
	public static CoinValue easyBuild2(IInventory inventory)
	{
		return new CoinValue(MoneyUtil.getValue(inventory));
	}
	
	public static CoinValue Parse(JsonElement json) throws Exception
	{
		if(json.isJsonPrimitive())
		{
			JsonPrimitive primitive = json.getAsJsonPrimitive();
			if(primitive.isNumber())
			{
				return new CoinValue(primitive.getAsNumber().longValue());
			}
			else if(primitive.isBoolean() && primitive.getAsBoolean())
			{
				CoinValue val = new CoinValue();
				val.setFree(true);
				return val;
			}
			else if(primitive.isString())
			{
				double displayValue = Double.parseDouble(primitive.getAsString());
				return MoneyUtil.displayValueToCoinValue(displayValue);
			}
		}
		else if(json.isJsonArray())
		{
			List<CoinValuePair> pairs = Lists.newArrayList();
			JsonArray list = json.getAsJsonArray();
			for(int i = 0; i < list.size(); ++i)
			{
				JsonObject coinData = list.get(i).getAsJsonObject();
				Item coinItem = Items.AIR;
				int quantity = 1;
				if(coinData.has("Coin"))
					coinItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(coinData.get("Coin").getAsString()));
				if(coinData.has("Count"))
					quantity = coinData.get("Count").getAsInt();
				if(quantity <= 0)
					LightmansCurrency.LogWarning("Coin Count (" + quantity + ") is <= 0. Entry will be ignored.");
				else if(!MoneyUtil.isCoin(coinItem))
					LightmansCurrency.LogWarning("Coin Item (" + ForgeRegistries.ITEMS.getKey(coinItem) + ") is not a valid coin. Entry will be ignored.");
				else
					pairs.add(new CoinValuePair(coinItem, quantity));
			}
			if(pairs.size() == 0)
				throw new Exception("Coin Value entry has no valid coin/count entries to parse.");
			return new CoinValue(pairs, true);
		}
		throw new Exception("Coin Value entry input is not a valid Json Element.");
	}
	
	public JsonElement toJson() {
		if(this.isFree)
			return new JsonPrimitive(true);
		else
		{
			JsonArray array = new JsonArray();
			for (CoinValuePair pair : this.coinValues) {
				JsonObject entry = new JsonObject();
				entry.addProperty("Coin", ForgeRegistries.ITEMS.getKey(pair.coin).toString());
				entry.addProperty("Count", pair.amount);
				array.add(entry);
			}
			return array;
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.isFree, this.coinValues);
	}
	
	@Override
	public boolean equals(Object other) {
		if(this == other)
			return true;
		else if(!(other instanceof CoinValue))
			return false;
		else
		{
			CoinValue coinValue = (CoinValue)other;
			if(coinValue.isFree && this.isFree)
				return true;
			else
				return coinValue.getRawValue() == this.getRawValue();
		}
	}
	
}