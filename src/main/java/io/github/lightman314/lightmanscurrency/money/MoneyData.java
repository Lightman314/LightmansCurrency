package io.github.lightman314.lightmanscurrency.money;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.events.GetDefaultMoneyDataEvent;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MoneyData {

	private List<CoinData> coinList = new ArrayList<>();
	
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
		for(int i = 0; i < this.coinList.size(); ++i)
		{
			try {
				JsonObject entry = this.coinList.get(i).toJson();
				dataList.add(entry);
			} catch(Throwable t) {}
		}
		json.add("CoinEntries", dataList);
		return json;
	}
	
	public static void encode(MoneyData data, PacketBuffer buffer) {
		JsonObject json = data.toJson();
		String jsonString = FileUtil.GSON.toJson(json);
		int stringSize = jsonString.length();
		buffer.writeInt(stringSize);
		buffer.writeString(jsonString, stringSize);
	}
	
	public static MoneyData decode(PacketBuffer buffer) {
		try {
			LightmansCurrency.LogInfo("Decoding money data packet:");
			int stringSize = buffer.readInt();
			String jsonString = buffer.readString(stringSize);
			JsonObject json = JSONUtils.fromJson(jsonString).getAsJsonObject();
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
    			LightmansCurrency.LogWarning("Attempted to add Duplicate Coin Item (" + newCoinData.coinItem.getRegistryName().toString() + ") to the coin list.");
    			//LightmansCurrency.LOGGER.warn("Please use MoneyUtil.changeCoinValue if you wish to change a coins value.");
    			return;
    		}
    		//Confirm that there's no duplicate dependent (Ignore this if either party is hidden, as conversion will be ignored for that coin)
    		if(coinData.worthOtherCoin == newCoinData.worthOtherCoin && !newCoinData.isHidden && !coinData.isHidden && newCoinData.worthOtherCoin != null && coinData.chain.contentEquals(newCoinData.chain))
    		{
    			LightmansCurrency.LogWarning("Attempted to add a new Coin Item '" + newCoinData.coinItem.getRegistryName() + "' with the same dependent (" + coinData.worthOtherCoin.getRegistryName() + ") as another coin (" + coinData.coinItem.getRegistryName() + ") in the same chain '" + coinData.chain + "'.\nEntry will be flagged as hidden.");
    			newCoinData = newCoinDataBuilder.setHidden().build();
    		}
    	}
    	LightmansCurrency.LogInfo("Registered " + newCoinData.coinItem.getRegistryName() + " as a coin.");
    	coinList.add(newCoinData);
    	
    }
	
	private void sortCoinList()
    {
    	List<CoinData> newList = new ArrayList<>();
    	while(coinList.size() > 0)
    	{
    		int highestValueIndex = 0;
    		long highestValue = coinList.get(0).getValue(this);
    		for(int i = 1; i < coinList.size(); i++)
    		{
    			if(coinList.get(i).getValue(this) > highestValue)
    			{
    				highestValueIndex = i;
    				highestValue = coinList.get(i).getValue(this);
    			}
    		}
    		newList.add(coinList.get(highestValueIndex));
    		coinList.remove(highestValueIndex);
    	}
    	coinList = newList;
    }
	
	public CoinData getData(Item coinItem) {
		for(CoinData data : this.coinList) {
			if(data.coinItem == coinItem)
				return data;
		}
		return null;
	}
	
	public List<CoinData> getCoinList() {
		return this.coinList;
	}
	
	public List<CoinData> getCoinList(String chain) {
		List<CoinData> results = new ArrayList<>();
		for(int i = 0; i < this.coinList.size(); ++i)
		{
			CoinData data = this.coinList.get(i);
			if(data.chain.contentEquals(chain))
				results.add(data);
		}
		return results;
	}
	
	public static class CoinDataCollector
	{
		List<CoinData.Builder> coinBuilderList = new ArrayList<>();
		
		public void addCoinBuilder(CoinData.Builder coinBuilder) {
			this.coinBuilderList.add(coinBuilder);
		}
	    
	}
	
}
