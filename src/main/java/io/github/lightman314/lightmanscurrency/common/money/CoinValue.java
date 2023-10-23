package io.github.lightman314.lightmanscurrency.common.money;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class CoinValue
{

	public enum ValueType { DEFAULT, VALUE }

	public static final CoinValue EMPTY = new CoinValue();
	public static final CoinValue FREE = new CoinValue() { @Override public boolean isFree() { return true; } };

	public boolean isFree() { return false; }
	public final ImmutableList<CoinValuePair> coinValues;

	public boolean hasAny() { return this.coinValues.size() > 0; }

	public boolean isValid() { return this.isFree() || this.coinValues.size() > 0; }

	private CoinValue() { this.coinValues = ImmutableList.of(); }
	private CoinValue(List<CoinValuePair> values) { this.coinValues = ImmutableList.copyOf(roundValue(values)); }

	public static CoinValue create(@Nonnull List<CoinValuePair> coinValues) { return coinValues.size() == 0 ? EMPTY : new CoinValue(coinValues); }


	//New save method for potential chain handling in the future
	@Nonnull
	public CompoundTag save()
	{
		CompoundTag compound = new CompoundTag();
		if(this.isFree())
			compound.putBoolean("Free", true);
		else
		{
			ListTag valueList = new ListTag();
			for(CoinValuePair pair : this.coinValues)
				valueList.add(pair.save());
			compound.put("Value", valueList);
		}
		return compound;
	}

	@Nonnull
	public static CoinValue load(CompoundTag compound)
	{
		if(compound.contains("Free", Tag.TAG_BYTE))
			return FREE;
		else if(compound.contains("Value", Tag.TAG_LIST))
		{
			ListTag valueList = compound.getList("Value", Tag.TAG_COMPOUND);
			List<CoinValuePair> pairList = new ArrayList<>();
			for(int i = 0; i < valueList.size(); ++i)
			{
				try{ pairList.add(CoinValuePair.load(valueList.getCompound(i)));
				} catch(Throwable ignored) {}
			}
			return create(pairList);
		}
		return EMPTY;
	}

	@Nonnull
	public static CoinValue safeLoad(CompoundTag compound, String key)
	{
		if(compound.contains(key, Tag.TAG_COMPOUND))
			return load(compound.getCompound(key));
		else if(compound.contains(key, Tag.TAG_INT))
			return fromNumber(compound.getInt(key));
		else if(compound.contains(key, Tag.TAG_LIST))
		{
			//Read full value using old method
			ListTag listNBT = compound.getList(key, Tag.TAG_COMPOUND);
			List<CoinValuePair> pairList = new ArrayList<>();
			for(int i = 0; i < listNBT.size(); i++)
			{
				CompoundTag thisCompound = listNBT.getCompound(i);
				Item priceCoin = ForgeRegistries.ITEMS.getValue(new ResourceLocation(thisCompound.getString("id")));
				int amount = thisCompound.getInt("amount");
				pairList.add(new CoinValuePair(priceCoin,amount));
			}
			return create(pairList);
		}
		else if(compound.contains(key, Tag.TAG_BYTE))
			return FREE;
		return EMPTY;
	}

	public static CoinValue fromNumber(long valueNumber)
	{
		if(valueNumber <= 0)
			return EMPTY;
		List<ItemStack> coinItems = MoneyUtil.getCoinsOfValue(valueNumber);
		List<CoinValuePair> pairList = new ArrayList<>();
		for(ItemStack stack : coinItems)
		{
			Item coinItem = stack.getItem();
			int amount = stack.getCount();
			for(int i = 0; i < pairList.size(); ++i)
			{
				if(pairList.get(i).coin == coinItem)
				{
					pairList.set(i, pairList.get(i).addAmount(amount));
					amount = 0;
				}
			}
			if(amount > 0)
				pairList.add(new CoinValuePair(coinItem,amount));
		}
		return create(pairList);
	}

	public static CoinValue fromInventory(List<ItemStack> inventory)
	{
		long value = 0;
		for (ItemStack itemStack : inventory)
			value += MoneyUtil.getValue(itemStack);
		return fromNumber(value);
	}

	public static CoinValue fromInventory(Container inventory)
	{
		long value = 0;
		for(int i = 0; i < inventory.getContainerSize(); ++i)
			value += MoneyUtil.getValue(inventory.getItem(i));
		return fromNumber(value);
	}

	/**
	 * Gets a non-empty coin value from either the value of the item,
	 * or if the item is not a registered coin it falls back onto the given number value.
	 */
	@Nonnull
	public static CoinValue fromItemOrValue(Item coin, long value) { return fromItemOrValue(coin, 1, value); }

	@Nonnull
	public static CoinValue fromItemOrValue(Item coin, int itemCount, long value)
	{
		long coinValue = MoneyUtil.getValue(coin);
		if(coinValue > 0)
			return fromNumber(coinValue * itemCount);
		return fromNumber(value);
	}

	public final void encode(FriendlyByteBuf buffer) {
		buffer.writeBoolean(this.isFree());
		if(!this.isFree())
		{
			buffer.writeInt(this.coinValues.size());
			for(CoinValuePair pair : this.coinValues)
				pair.encode(buffer);
		}
	}

	public static CoinValue decode(FriendlyByteBuf buffer)
	{
		if(buffer.readBoolean())
			return FREE;
		int pairCount = buffer.readInt();
		if(pairCount <= 0)
			return EMPTY;
		List<CoinValuePair> pairList = new ArrayList<>();
		while(pairCount-- > 0)
			pairList.add(CoinValuePair.decode(buffer));
		return create(pairList);
	}

	@Nonnull
	public CoinValue plusValue(@Nonnull CoinValue otherValue) { return fromNumber(this.getValueNumber() + otherValue.getValueNumber()); }

	@Nonnull
	public CoinValue plusValue(@Nonnull Item coin, int amount) { return fromNumber(this.getValueNumber() + MoneyUtil.getValue(coin) * amount); }

	@Nonnull
	public CoinValue minusValue(@Nonnull CoinValue otherValue) { return fromNumber(this.getValueNumber() - otherValue.getValueNumber()); }

	@Nonnull
	public CoinValue minusValue(@Nonnull Item coin, int amount) { return fromNumber(this.getValueNumber() - MoneyUtil.getValue(coin) * amount); }


	@Nonnull
	public CoinValue percentageOfValue(int percentage) { return this.percentageOfValue(percentage, false); }
	@Nonnull
	public CoinValue percentageOfValue(int percentage, boolean roundUp)
	{
		if(percentage == 100)
			return this;
		if(percentage == 0)
			return FREE;
		long value = this.getValueNumber();
		//Calculate the new value
		long newValue = value * MathUtil.clamp(percentage, 0, Integer.MAX_VALUE) / 100L;
		//Calculate the new value in double format for rounding checks
		if(roundUp)
		{
			double newValueD = (value * ((double)percentage/100d));
			//Check if we should round
			if(roundUp && newValueD % 1d != 0d && newValueD > newValue)
				newValue += 1;
		}
		if(newValue == 0)
			return FREE;
		return fromNumber(newValue);
	}

	//Rounding and Sorting functions. Now static and only used on a coin values init stage as they are now immutable.
	private static List<CoinValuePair> roundValue(List<CoinValuePair> list)
	{
		while(needsRounding(list))
		{
			for(int i = 0; i < list.size(); i++)
			{
				if(needsRounding(list, i))
				{
					CoinValuePair pair = list.get(i);
					Pair<Item,Integer> conversion = MoneyUtil.getUpwardConversion(pair.coin);
					int largeAmount = 0;
					while(pair.amount >= conversion.getSecond())
					{
						largeAmount++;
						pair = pair.removeAmount(conversion.getSecond());
					}
					//If there's none of this coin left, remove if from the list
					if(pair.amount == 0)
					{
						list.remove(i);
						//Shrink the index to avoid oversight
						i--;
					}
					//Otherwise replace the list entry with the new input
					else
						list.set(i, pair);
					//Add the larger amount to the price values list
					for(int j = 0; j < list.size(); ++j)
					{
						if(list.get(j).coin == conversion.getFirst())
						{
							list.set(j, list.get(j).addAmount(largeAmount));
							largeAmount = 0;
						}
					}
					if(largeAmount > 0)
					{
						list.add(new CoinValuePair(conversion.getFirst(), largeAmount));
					}
				}
			}
		}
		return sortValue(list);
	}

	private static List<CoinValuePair> sortValue(List<CoinValuePair> list)
	{
		List<CoinValuePair> newList = new ArrayList<>();
		while(list.size() > 0)
		{
			//Get the largest index
			long largestValue = MoneyUtil.getValue(list.get(0).coin);
			int largestIndex = 0;
			for(int i = 1; i < list.size(); i++)
			{
				long thisValue = MoneyUtil.getValue(list.get(i).coin);
				if(thisValue > largestValue)
				{
					largestIndex = i;
					largestValue = thisValue;
				}
			}
			newList.add(list.get(largestIndex));
			list.remove(largestIndex);
		}
		return newList;
	}

	private static boolean needsRounding(List<CoinValuePair> list)
	{
		for(int i = 0; i < list.size(); i++)
		{
			if(needsRounding(list, i))
				return true;
		}
		return false;
	}

	private static boolean needsRounding(List<CoinValuePair> list, int index)
	{
		CoinValuePair pair = list.get(index);
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

	//No need for a copy function, as Coin Values are now Immutable

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

	public String getString(String emptyFiller) { return getComponent(emptyFiller).getString(); }

	public MutableComponent getComponent() { return this.getComponent(""); }

	public MutableComponent getComponent(String emptyFiller) { return getComponent(EasyText.literal(emptyFiller)); }
	public MutableComponent getComponent(MutableComponent emptyFiller)
	{
		if(this.isFree())
			return EasyText.translatable("gui.coinvalue.free");

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
				if(string.toString().isBlank())
					return emptyFiller;
				return EasyText.literal(string.toString());
			case VALUE:
				return EasyText.literal(Config.formatValueDisplay(this.getDisplayValue()));
			default:
				return EasyText.literal("?");
		}
	}

	public long getValueNumber()
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

	public static class CoinValuePair
	{

		public final Item coin;
		public final int amount;

		public CoinValuePair addAmount(int amount) { return new CoinValuePair(this.coin, this.amount + amount); }
		public CoinValuePair removeAmount(int amount) { return new CoinValuePair(this.coin, this.amount - amount); }

		public CoinValuePair(Item coin, int amount)
		{
			this.coin = coin;
			this.amount = amount;
		}

		public CoinValuePair copy() { return new CoinValuePair(this.coin,this.amount); }

		public CompoundTag save()
		{
			CompoundTag compound = new CompoundTag();
			compound.putString("Coin", ForgeRegistries.ITEMS.getKey(this.coin).toString());
			compound.putInt("Amount", this.amount);
			return compound;
		}

		public static CoinValuePair load(CompoundTag compound) { return from(compound.getString("Coin"), compound.getInt("Amount")); }

		public void encode(FriendlyByteBuf buffer)
		{
			buffer.writeUtf(ForgeRegistries.ITEMS.getKey(this.coin).toString());
			buffer.writeInt(this.amount);
		}

		public static CoinValuePair decode(FriendlyByteBuf buffer) { return from(buffer.readUtf(), buffer.readInt()); }

		public JsonObject toJson()
		{
			JsonObject json = new JsonObject();
			json.addProperty("Coin", ForgeRegistries.ITEMS.getKey(this.coin).toString());
			json.addProperty("Amount", this.amount);
			return json;
		}

		@Nonnull
		public static CoinValuePair fromJson(JsonObject json) throws Exception
		{
			Item coinItem = Items.AIR;
			int quantity = 1;
			if(json.has("Coin"))
				coinItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(json.get("Coin").getAsString()));
			if(json.has("Amount"))
				quantity = json.get("Amount").getAsInt();
			if(quantity <= 0)
				throw new RuntimeException("Coin Amount (" + quantity + ") is <= 0!");
			else if(!MoneyUtil.isCoin(coinItem))
				throw new RuntimeException("Coin Item (" + ForgeRegistries.ITEMS.getKey(coinItem) + ") is not a valid coin!");
			return new CoinValuePair(coinItem, quantity);
		}

		private static CoinValuePair from(String itemID, int amount) { return new CoinValuePair(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID)), amount); }

	}

	public JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		if(this.isFree())
			json.addProperty("Free", true);
		else
		{
			JsonArray array = new JsonArray();
			for (CoinValuePair pair : this.coinValues)
				array.add(pair.toJson());
			json.add("Value", array);
		}
		return json;
	}

	public static CoinValue Parse(JsonElement json) throws Exception
	{
		if(json.isJsonPrimitive())
		{
			JsonPrimitive primitive = json.getAsJsonPrimitive();
			if(primitive.isNumber())
			{
				return fromNumber(primitive.getAsNumber().longValue());
			}
			else if(primitive.isBoolean() && primitive.getAsBoolean())
			{
				return FREE;
			}
			else if(primitive.isString())
			{
				double displayValue = Double.parseDouble(primitive.getAsString());
				return MoneyUtil.displayValueToCoinValue(displayValue);
			}
		}
		else if(json.isJsonArray())
		{

			JsonArray list = json.getAsJsonArray();
			List<CoinValuePair> valuePairs = new ArrayList<>();
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
					valuePairs.add(new CoinValuePair(coinItem, quantity));
			}
			if(valuePairs.size() == 0)
				throw new Exception("Coin Value entry has no valid coin/count entries to parse.");
			return create(valuePairs);
		}
		else if(json.isJsonObject())
		{
			JsonObject j = json.getAsJsonObject();
			if(j.has("Free"))
			{
				JsonElement f = j.get("Free");
				if(f.isJsonPrimitive())
				{
					JsonPrimitive f2 = f.getAsJsonPrimitive();
					if(f2.isBoolean() && f2.getAsBoolean())
						return FREE;
				}
			}
			if(j.has("Value"))
			{
				JsonArray valueList = j.get("Value").getAsJsonArray();
				List<CoinValuePair> pairs = new ArrayList<>();
				for(int i = 0; i < valueList.size(); ++i)
				{
					try{ pairs.add(CoinValuePair.fromJson(valueList.get(i).getAsJsonObject()));
					} catch(Throwable t) { LightmansCurrency.LogError("Error Parsing Coin Value Entry #" + (i+1), t); }
				}
				return create(pairs);
			}
		}
		throw new Exception("Coin Value entry input is not a valid Json Element.");
	}

	@Override
	public int hashCode() { return Objects.hashCode(this.isFree(), this.coinValues); }

	@Override
	public boolean equals(Object other) {
		if(this == other)
			return true;
		if(other instanceof CoinValue coinValue)
			return coinValue.getValueNumber() == this.getValueNumber() && coinValue.isFree() == this.isFree();
		return false;
	}

}