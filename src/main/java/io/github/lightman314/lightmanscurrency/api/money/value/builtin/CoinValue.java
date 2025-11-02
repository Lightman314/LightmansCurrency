package io.github.lightman314.lightmanscurrency.api.money.value.builtin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.CoinCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.display.CoinPriceEntry;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CoinValue extends MoneyValue implements IItemBasedValue
{

	public final ImmutableList<CoinValuePair> coinValues;

	private final String chain;
	private CompoundTag backup = null;
	@Nonnull
	public String getChain() { return this.chain; }

	@Override
	public boolean isEmpty() { return this.coinValues.isEmpty(); }
	@Override
	public boolean isInvalid() { return this.backup != null; }

	public boolean isValid() { return this.isFree() || !this.coinValues.isEmpty(); }

	@Nonnull
	@Override
	protected String generateUniqueName() { return this.generateCustomUniqueName(this.chain); }

	@Nonnull
	@Override
	protected ResourceLocation getType() { return CoinCurrencyType.TYPE; }

	private CoinValue(@Nonnull String chain, @Nonnull CompoundTag backup)
	{
		this.chain = chain;
		this.coinValues = ImmutableList.of();
		this.backup = backup;
	}
	private CoinValue(@Nonnull String chain, @Nonnull List<CoinValuePair> values) {
		this.chain = chain;
		this.coinValues = ImmutableList.copyOf(roundValue(this.chain, values));
	}


	public static MoneyValue create(@Nonnull String chain, @Nonnull List<CoinValuePair> coinValues) { return coinValues.isEmpty() ? MoneyValue.empty() : new CoinValue(chain, coinValues); }


	public void saveAdditional(@Nonnull CompoundTag tag)
	{
		if(this.backup != null)
		{
			tag.merge(this.backup);
			return;
		}
		tag.putString("Chain", this.chain);
		ListTag valueList = new ListTag();
		for(CoinValuePair pair : this.coinValues)
			valueList.add(pair.save());
		tag.put("Value", valueList);
	}

	@Nonnull
	public static MoneyValue loadCoinValue(@Nonnull CompoundTag tag)
	{
		if(tag.contains("Chain", Tag.TAG_STRING))
		{
			boolean dataLoaded = true;
			if(CoinAPI.getApi().NoDataAvailable())
			{
				LightmansCurrency.LogWarning("Coin Value loaded before receiving the Chain Data packet. Will assume all value pairs are valid and don't need rounding!");
				dataLoaded = false;
			}
			String chain = tag.getString("Chain");
			ChainData chainData = CoinAPI.getApi().ChainData(chain);
			if(chainData == null && dataLoaded) //Load value as backup state, so that if the config gets reloaded properly and this chain gets re-added
				return new CoinValue(chain, tag);
			if(tag.contains("Value", Tag.TAG_LIST))
			{
				ListTag valueList = tag.getList("Value", Tag.TAG_COMPOUND);
				List<CoinValuePair> pairList = new ArrayList<>();
				for(int i = 0; i < valueList.size(); ++i)
				{
					try{ pairList.add(CoinValuePair.load(chainData, valueList.getCompound(i)));
					} catch(RuntimeException ignored) {}
				}
				return create(chain, pairList);
			}
		}
		return MoneyValue.empty();
	}

	/**
	 * Use {@link MoneyValue#load(CompoundTag)} instead.
	 */
	@Nullable
	public static MoneyValue loadDeprecated(@Nonnull CompoundTag tag)
	{
		if(tag.contains("Free", Tag.TAG_BYTE))
			return MoneyValue.free();
		else if(tag.contains("Value", Tag.TAG_LIST))
		{
			ListTag valueList = tag.getList("Value", Tag.TAG_COMPOUND);
			if(valueList.isEmpty())
				return MoneyValue.empty();

			List<CoinValuePair> pairList = new ArrayList<>();
			ChainData chainData = null;
			for(int i = 0; i < valueList.size(); ++i)
			{
				CompoundTag entry = valueList.getCompound(i);
				Item coin = ForgeRegistries.ITEMS.getValue(VersionUtil.parseResource(entry.getString("Coin")));
				int amount = entry.getInt("Amount");
				if(chainData == null)
					chainData = CoinAPI.getApi().ChainDataOfCoin(coin);
				if(chainData != null && chainData.containsEntry(coin))
					pairList.add(new CoinValuePair(coin, amount));
			}
			if(chainData != null)
				return create(chainData.chain, pairList);
		}
		return MoneyValue.empty();
	}

	/**
	 * Use {@link MoneyValue#safeLoad(CompoundTag, String)} instead.
	 */
	@Nullable
	public static MoneyValue loadDeprecated(@Nonnull CompoundTag parentTag, @Nonnull String key)
	{
		if(parentTag.contains(key, Tag.TAG_INT))
			return fromNumber("main", parentTag.getInt(key));
		else if(parentTag.contains(key, Tag.TAG_LIST))
		{
			//Read full value using old method
			ListTag listNBT = parentTag.getList(key, Tag.TAG_COMPOUND);
			if(listNBT.isEmpty())
				return MoneyValue.empty();
			List<CoinValuePair> pairList = new ArrayList<>();
			ChainData chainData = null;
			for(int i = 0; i < listNBT.size(); i++)
			{
				CompoundTag thisCompound = listNBT.getCompound(i);
				Item coin = ForgeRegistries.ITEMS.getValue(VersionUtil.parseResource(thisCompound.getString("id")));
				int amount = thisCompound.getInt("amount");
				if(chainData == null)
					chainData = CoinAPI.getApi().ChainDataOfCoin(coin);
				if(chainData != null && chainData.containsEntry(coin))
					pairList.add(new CoinValuePair(coin,amount));
			}
			if(chainData != null)
				return create(chainData.chain, pairList);
		}
		else if(parentTag.contains(key, Tag.TAG_BYTE) && parentTag.getBoolean(key))
			return MoneyValue.free();
		return MoneyValue.empty();
	}

	public static MoneyValue fromNumber(@Nonnull String chain, long valueNumber) { return fromNumber(CoinAPI.getApi().ChainData(chain), valueNumber); }
	public static MoneyValue fromNumber(ChainData chainData, long valueNumber)
	{
		//LightmansCurrency.LogDebug("Generating Coin Value from '" + chain + "' with a value of " + valueNumber);
		if(valueNumber <= 0)
		{
			//LightmansCurrency.LogDebug("Value was <= 0. Returning empty.");
			return MoneyValue.empty();
		}
		if(chainData == null)
		{
			//LightmansCurrency.LogDebug("Chain does not exist. Returning empty.");
			return MoneyValue.empty();
		}
		long pendingValue = valueNumber;
		List<CoinEntry> entries = chainData.getAllEntries(false, ChainData.SORT_HIGHEST_VALUE_FIRST);
		List<CoinValuePair> pairList = new ArrayList<>();
		for(CoinEntry entry : entries)
		{
			long entryValue = entry.getCoreValue();
			if(pendingValue >= entryValue && entryValue != 0)
			{
				//LightmansCurrency.LogDebug("Comparing pending value of " + pendingValue + " to the value of " + new ItemStack(entry.getCoin()).getHoverName().getString() + " (" + entryValue + ")");
				long thisCount = pendingValue / entryValue;
				pendingValue = (pendingValue % entryValue);
				if(thisCount > 0)
				{
					int intCount;
					if(thisCount >= Integer.MAX_VALUE)
					{
						LightmansCurrency.LogWarning("Value count of " + new ItemStack(entry.getCoin()).getHoverName().getString() + " is greater than the maximum integer value!");
						intCount = Integer.MAX_VALUE;
					}
					else
						intCount = (int)thisCount;
					pairList.add(new CoinValuePair(entry.getCoin(), intCount));
				}
				if(pendingValue <= 0)
					break;
			}
		}
		return create(chainData.chain,pairList);
	}

	/**
	 * Gets a non-empty coin value from either the value of the item,
	 * or if the item is not a registered coin it falls back onto the given number value.
	 */
	@Nonnull
	public static MoneyValue fromItemOrValue(Item coin, long value) { return fromItemOrValue(coin, 1, value); }

	@Nonnull
	public static MoneyValue fromItemOrValue(Item coin, int itemCount, long value)
	{
		ChainData chainData = CoinAPI.getApi().ChainDataOfCoin(coin);
		if(chainData != null)
			return new CoinValue(chainData.chain, Lists.newArrayList(new CoinValuePair(coin, itemCount)));
		return fromNumber("main", value);
	}

	@Override
	public MoneyValue addValue(@Nonnull MoneyValue addedValue) {
		if(this.sameType(addedValue))
			return fromNumber(this.chain, this.getCoreValue() + addedValue.getCoreValue());
		return null;
	}

	@Override
	public boolean containsValue(@Nonnull MoneyValue queryValue) {
		if(this.sameType(queryValue))
			return this.getCoreValue() >= queryValue.getCoreValue();
		return false;
	}

	@Override
	public MoneyValue subtractValue(@Nonnull MoneyValue removedValue) {
		if(this.sameType(removedValue) && this.containsValue(removedValue))
			return fromNumber(this.chain, this.getCoreValue() - removedValue.getCoreValue());
		return null;
	}

	@Nonnull
	@Override
	public MoneyValue multiplyValue(double multiplier) {
		BigDecimal value = BigDecimal.valueOf(this.getCoreValue());
		BigDecimal result = value.multiply(BigDecimal.valueOf(multiplier));
		//If less than 1, return empty
		if(result.compareTo(BigDecimal.valueOf(0.5d)) < 0)
			return MoneyValue.empty();
		if(result.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0)
		{
			//If larger than max long value, return max long value
			return fromNumber(this.chain, Long.MAX_VALUE);
		}
		long rounding = 0;
		if(result.remainder(BigDecimal.ONE).compareTo(BigDecimal.valueOf(0.5d)) >= 0)
			rounding = 1;
		return fromNumber(this.chain, result.longValue() + rounding);
	}

	@Override
	public MoneyValue percentageOfValue(int percentage, boolean roundUp) {
		if(percentage == 100)
			return this;
		if(percentage == 0)
			return MoneyValue.free();
		long value = this.getCoreValue();
		//Calculate the new value
		long newValue = value * MathUtil.clamp(percentage, 0, 1000) / 100L;
		//Calculate the new value in double format for rounding checks
		if(roundUp)
		{
			long partial = value * MathUtil.clamp(percentage, 0, 1000) % 100L;
			if(partial > 0)
				newValue += 1;
		}
		if(newValue == 0)
			return MoneyValue.free();
		return fromNumber(this.chain, newValue);
	}

	@Nonnull
	@Override
	public MoneyValue getSmallestValue() { return fromNumber(this.chain, 1); }

	@Nonnull
	@Override
	public MoneyValue fromCoreValue(long value) { return fromNumber(this.chain, value); }

	@Nonnull
	@Override
	public List<ItemStack> onBlockBroken(@Nonnull OwnerData owner) { return this.getAsSeperatedItemList(); }

	//Rounding and Sorting functions. Now static and only used on a coin values init stage as they are now immutable.
	private static List<CoinValuePair> roundValue(@Nonnull String chain, @Nonnull List<CoinValuePair> list)
	{
		ChainData chainData = CoinAPI.getApi().ChainData(chain);
		if(chainData == null)
			return list;
		while(needsRounding(chainData, list))
		{
			for(int i = 0; i < list.size(); i++)
			{
				if(needsRounding(chainData, list, i))
				{
					CoinValuePair pair = list.get(i);
					CoinEntry entry = chainData.findEntry(pair.coin);
					Pair<CoinEntry,Integer> exchange = entry.getUpperExchange();
					int largeAmount = 0;
					while(pair.amount >= exchange.getSecond())
					{
						largeAmount++;
						pair = pair.removeAmount(exchange.getSecond());
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
						if(exchange.getFirst().matches(list.get(j).coin))
						{
							list.set(j, list.get(j).addAmount(largeAmount));
							largeAmount = 0;
						}
					}
					if(largeAmount > 0)
					{
						list.add(new CoinValuePair(exchange.getFirst().getCoin(), largeAmount));
					}
				}
			}
		}
		return sortValue(chainData, list);
	}
	
	private static List<CoinValuePair> sortValue(@Nonnull ChainData chainData, List<CoinValuePair> list)
	{
		List<CoinValuePair> newList = new ArrayList<>();
		while(!list.isEmpty())
		{
			//Get the largest index
			long largestValue = chainData.getCoreValue(list.get(0).coin);
			int largestIndex = 0;
			for(int i = 1; i < list.size(); i++)
			{
				long thisValue = chainData.getCoreValue(list.get(i).coin);
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
	
	private static boolean needsRounding(@Nonnull ChainData chainData, @Nonnull List<CoinValuePair> list)
	{
		for(int i = 0; i < list.size(); i++)
		{
			if(needsRounding(chainData, list, i))
				return true;
		}
		return false;
	}
	
	private static boolean needsRounding(@Nonnull ChainData chainData, @Nonnull List<CoinValuePair> list, int index)
	{
		CoinValuePair pair = list.get(index);
		Pair<CoinEntry,Integer> exchange = chainData.getUpperExchange(pair.coin);
		if(exchange != null)
			return pair.amount >= exchange.getSecond();
		return false;
	}
	
	public List<CoinValuePair> getEntries() { return this.coinValues; }

	@Nonnull
	public List<ItemStack> getAsItemList() {
		List<ItemStack> items = new ArrayList<>();
		for(CoinValuePair entry : this.coinValues)
			items.add(new ItemStack(entry.coin,entry.amount));
		return items;
	}
	
	public long getEntry(Item coinItem)
	{
		for(CoinValuePair pair : this.coinValues)
		{
			if(pair.coin == coinItem)
				return pair.amount;
		}
		return 0;
	}

	@Nonnull
	@Override
	public MutableComponent getText(@Nonnull MutableComponent emptyText)
	{
		ChainData chainData = CoinAPI.getApi().ChainData(this.chain);
		if(chainData == null)
			return EasyText.literal("ERROR");
		else
			return chainData.formatValue(this, emptyText);
	}

	@Override
	public long getCoreValue() {
		ChainData chainData = CoinAPI.getApi().ChainData(this.chain);
		if(chainData == null)
			return 0;
		long value = 0;
		for(CoinValuePair pricePair : this.coinValues)
			value += chainData.getCoreValue(pricePair.coin) * pricePair.amount;
		//LightmansCurrency.LOGGER.info("Accumulated Value: " + value + " PricePair count: " + this.priceValues.size());
		//Quick test to make 100% certain that no negative numbers are passed
		return Math.max(0,value);
	}

	@Override
	protected void writeAdditionalToJson(@Nonnull JsonObject json) {
		JsonArray array = new JsonArray();
		for (CoinValuePair pair : this.coinValues)
			array.add(pair.toJson());
		json.add("Value", array);
		json.addProperty("Chain", this.chain);
	}

	public static MoneyValue loadCoinValue(@Nonnull JsonObject json) throws JsonSyntaxException, ResourceLocationException {
		String chain = GsonHelper.getAsString(json, "Chain");
		ChainData data = CoinAPI.getApi().ChainData(chain);
		if(data == null)
			throw new JsonSyntaxException("No " + chain + " chain has been registered!");
		List<CoinValuePair> valuePairs = new ArrayList<>();
		JsonArray entryArray = GsonHelper.getAsJsonArray(json, "Value");
		for(int i = 0; i < entryArray.size(); ++i)
		{
			try { valuePairs.add(CoinValuePair.fromJson(data, GsonHelper.convertToJsonObject(entryArray.get(i),"Value[" + i + "]")));
			} catch (JsonSyntaxException | ResourceLocationException e) { LightmansCurrency.LogError("Error parsing Coin Value entry #" + (i + 1) + "!", e); }
		}
		if(valuePairs.isEmpty())
			throw new JsonSyntaxException("Coin Value entry has no valid coin/count entries to parse.");
		return create(chain, valuePairs);
	}
	
	public static MoneyValue loadDeprecated(JsonElement json) throws JsonSyntaxException, ResourceLocationException
	{
		if(json.isJsonPrimitive())
		{
			JsonPrimitive primitive = json.getAsJsonPrimitive();
			if(primitive.isNumber())
			{
				return fromNumber(CoinAPI.MAIN_CHAIN, primitive.getAsNumber().longValue());
			}
			else if(primitive.isBoolean() && primitive.getAsBoolean())
			{
				return MoneyValue.empty();
			}
			else if(primitive.isString())
			{
				double displayValue = Double.parseDouble(primitive.getAsString());
				ChainData mainChain = CoinAPI.getApi().ChainData(CoinAPI.MAIN_CHAIN);
				if(mainChain != null)
					return mainChain.getDisplayData().parseDisplayInput(displayValue);
			}
		}
		else if(json.isJsonArray())
		{
			JsonArray list = json.getAsJsonArray();
			List<CoinValuePair> valuePairs = new ArrayList<>();
			ChainData chainData = null;
			for(int i = 0; i < list.size(); ++i)
			{
				try {
					JsonObject coinData = list.get(i).getAsJsonObject();
					//Parse coin
					Item coin = ForgeRegistries.ITEMS.getValue(VersionUtil.parseResource(GsonHelper.getAsString(coinData, "Coin")));
					if(chainData == null)
						chainData = CoinAPI.getApi().ChainDataOfCoin(coin);
					//Parse count
					int quantity = GsonHelper.getAsInt(coinData, "Count", 1);
					if(quantity <= 0)
						throw new JsonSyntaxException("Count was less than 1");
					if(chainData == null)
						throw new JsonSyntaxException("Coin Item was not a valid coin!");
					if(chainData.findEntry(coin) == null)
						throw new JsonSyntaxException("Coin Item is not a valid coin, or is on a different chain from the rest of the value!");
					if(chainData.findEntry(coin).isSideChain())
						throw new JsonSyntaxException("Coin Item is from a side-chain, and thus cannot be used for value storage!");
					valuePairs.add(new CoinValuePair(coin, quantity));
				} catch (JsonSyntaxException | ResourceLocationException e) { LightmansCurrency.LogError("Error parsing CoinValue entry #" + (i + 1), e); }
			}
			if(valuePairs.isEmpty() || chainData == null)
				throw new JsonSyntaxException("Coin Value entry has no valid coin/count entries to parse.");
			return create(chainData.chain, valuePairs);
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
						return MoneyValue.free();
				}
			}
			if(j.has("Value"))
			{
				JsonArray valueList = j.get("Value").getAsJsonArray();
				ChainData chainData = null;
				List<CoinValuePair> pairs = new ArrayList<>();
				for(int i = 0; i < valueList.size(); ++i)
				{
					try {
						JsonObject coinData = valueList.get(i).getAsJsonObject();
						//Parse coin
						Item coin = ForgeRegistries.ITEMS.getValue(VersionUtil.parseResource(GsonHelper.getAsString(coinData, "Coin")));
						if(chainData == null)
							chainData = CoinAPI.getApi().ChainDataOfCoin(coin);
						//Parse count
						int quantity = GsonHelper.getAsInt(coinData, "Count", 1);
						if(quantity <= 0)
							throw new JsonSyntaxException("Count was less than 1");
						if(chainData == null)
							throw new JsonSyntaxException("Coin Item was not a valid coin!");
						if(chainData.findEntry(coin) == null)
							throw new JsonSyntaxException("Coin Item is not a valid coin, or is on a different chain from the rest of the value!");
						if(chainData.findEntry(coin).isSideChain())
							throw new JsonSyntaxException("Coin Item is from a side-chain, and thus cannot be used for value storage!");
						pairs.add(new CoinValuePair(coin, quantity));
					} catch (JsonSyntaxException | ResourceLocationException e) { LightmansCurrency.LogError("Error parsing CoinValue entry #" + (i + 1), e); }
				}
				if(pairs.isEmpty() || chainData == null)
					throw new JsonSyntaxException("Coin Value entry has no valid coin/count entries to parse.");
				return create(chainData.chain, pairs);
			}
		}
		throw new JsonSyntaxException("Coin Value entry input is not a valid Json Element.");
	}

	@Nonnull
	@Override
	public DisplayEntry getDisplayEntry(@Nullable List<Component> additionalTooltips, boolean tooltipOverride) { return new CoinPriceEntry(this, additionalTooltips, tooltipOverride); }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(CoinValuePair pair : this.coinValues)
		{
			if(!sb.isEmpty())
				sb.append(',');
			sb.append(pair.amount).append('x').append(ForgeRegistries.ITEMS.getKey(pair.coin));
		}
		return "CoinValue:" + sb;
	}
}