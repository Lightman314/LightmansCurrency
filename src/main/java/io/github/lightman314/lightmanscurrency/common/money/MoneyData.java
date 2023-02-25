package io.github.lightman314.lightmanscurrency.common.money;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.events.GetDefaultMoneyDataEvent;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.registries.ForgeRegistries;

public class MoneyData {

	private final List<CoinData> coinList = new ArrayList<>();
	private List<CoinData> sortedCoinList = new ArrayList<>();
	
	public MoneyData(CoinDataCollector collector)
	{
		for(CoinData.Builder dataBuilder : collector.coinBuilderList)
			this.addCoinItem(dataBuilder);
		this.sortCoinList();
	}
	
	public static MoneyData fromJson(JsonObject json) throws Throwable {
		JsonArray array = json.get("CoinEntries").getAsJsonArray();
		CoinDataCollector collector = new CoinDataCollector();
		for(int i = 0; i < array.size(); ++i)
		{
			try {
				CoinData.Builder builder = CoinData.getBuilder(array.get(i).getAsJsonObject());
				collector.addCoinBuilder(builder);
			} catch(Exception e) { LightmansCurrency.LogError("Error loading coin entry " + (i + 1), e); }
		}
		return new MoneyData(collector);
	}
	
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		JsonArray dataList = new JsonArray();
		for (CoinData coinData : this.coinList) {
			try {
				JsonObject entry = coinData.toJson();
				dataList.add(entry);
			} catch (Throwable ignored) { }
		}
		json.add("CoinEntries", dataList);
		return json;
	}
	
	public static void encode(MoneyData data, FriendlyByteBuf buffer) {
		JsonObject json = data.toJson();
		String jsonString = FileUtil.GSON.toJson(json);
		int stringSize = jsonString.length();
		buffer.writeInt(stringSize);
		buffer.writeUtf(jsonString, stringSize);
	}
	
	public static MoneyData decode(FriendlyByteBuf buffer) {
		try {
			LightmansCurrency.LogInfo("Decoding money data packet:");
			int stringSize = buffer.readInt();
			String jsonString = buffer.readUtf(stringSize);
			JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
			return fromJson(json);
		} catch(Throwable t) { LightmansCurrency.LogError("Error decoding MoneyData.", t); return generateDefault(); }
	}
	
	public static void handle(MoneyData data, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			LightmansCurrency.LogInfo("Received money data packet from server. Synchronizing coin list.");
			MoneyUtil.receiveMoneyData(data);
		});
		source.get().setPacketHandled(true);
	}
	
	public static MoneyData generateDefault() {
		GetDefaultMoneyDataEvent e = new GetDefaultMoneyDataEvent(new CoinDataCollector());
		MinecraftForge.EVENT_BUS.post(e);
		return new MoneyData(e.dataCollector);
	}
	
	/**
     * Adds a coin to the official coin list, thus allowing it to be acknowledged as a coin
     */
    private void addCoinItem(CoinData.Builder newCoinDataBuilder)
    {
    	CoinData newCoinData = newCoinDataBuilder.build();
    	for(CoinData coinData : coinList)
    	{
    		//Confirm that there's no duplicate coin
    		if(coinData.coinItem == newCoinData.coinItem)
    		{
    			LightmansCurrency.LogWarning("Attempted to add Duplicate Coin Item (" + ForgeRegistries.ITEMS.getKey(newCoinData.coinItem).toString() + ") to the coin list.");
    			//LightmansCurrency.LOGGER.warn("Please use MoneyUtil.changeCoinValue if you wish to change a coins value.");
    			return;
    		}
    		//Confirm that there's no duplicate dependent (Ignore this if either party is hidden, as conversion will be ignored for that coin)
    		if(coinData.worthOtherCoin == newCoinData.worthOtherCoin && !newCoinData.isHidden && !coinData.isHidden && newCoinData.worthOtherCoin != null && coinData.chain.contentEquals(newCoinData.chain))
    		{
    			LightmansCurrency.LogWarning("Attempted to add a new Coin Item '" + ForgeRegistries.ITEMS.getKey(newCoinData.coinItem) + "' with the same dependent (" + ForgeRegistries.ITEMS.getKey(coinData.worthOtherCoin) + ") as another coin (" + ForgeRegistries.ITEMS.getKey(coinData.coinItem) + ") in the same chain '" + coinData.chain + "'.\nEntry will be flagged as hidden.");
    			newCoinData = newCoinDataBuilder.setHidden().build();
    		}
    	}
    	LightmansCurrency.LogInfo("Registered " + ForgeRegistries.ITEMS.getKey(newCoinData.coinItem) + " as a coin.");
    	coinList.add(newCoinData);
    	
    }
	
	private void sortCoinList()
    {
    	this.sortedCoinList = new ArrayList<>();
    	List<CoinData> copyList = Lists.newArrayList(this.coinList);
    	while(copyList.size() > 0)
    	{
    		int highestValueIndex = 0;
    		long highestValue = copyList.get(0).getValue(this);
    		for(int i = 1; i < copyList.size(); i++)
    		{
    			if(copyList.get(i).getValue(this) > highestValue)
    			{
    				highestValueIndex = i;
    				highestValue = copyList.get(i).getValue(this);
    			}
    		}
    		sortedCoinList.add(copyList.get(highestValueIndex));
    		copyList.remove(highestValueIndex);
    	}
    }
	
	public MutableComponent getPluralName(Item coinItem) {
		CoinData data = this.getData(coinItem);
		if(data != null)
			return data.getPlural();
		else
			return MoneyUtil.getDefaultPlural(coinItem);
	}
	
	public CoinData getData(Item coinItem) {
		for(CoinData data : this.coinList) {
			if(data.coinItem == coinItem)
				return data;
		}
		return null;
	}
	
	public ImmutableList<CoinData> getSortedCoinList() { return ImmutableList.copyOf(this.sortedCoinList); }
	
	public ImmutableList<CoinData> getSortedCoinList(String chain) {
		List<CoinData> results = new ArrayList<>();
		for (CoinData data : this.sortedCoinList) {
			if (data.chain.contentEquals(chain))
				results.add(data);
		}
		return ImmutableList.copyOf(results);
	}
	
	public static class CoinDataCollector
	{
		List<CoinData.Builder> coinBuilderList = new ArrayList<>();
		
		public void addCoinBuilder(CoinData.Builder coinBuilder) {
			this.coinBuilderList.add(coinBuilder);
		}
	    
	}
	
}
