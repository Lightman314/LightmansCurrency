package io.github.lightman314.lightmanscurrency.tradedata.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;

public class PlayerTradeLimit extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_tradelimit");
	public static final ITradeRuleDeserializer<PlayerTradeLimit> DESERIALIZER = new Deserializer();
	
	private int limit = 1;
	public int getLimit() { return this.limit; }
	public void setLimit(int newLimit) { this.limit = newLimit; }
	
	Map<UUID,Integer> tradeHistory = new HashMap<>();
	
	public PlayerTradeLimit() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(getTradeCount(event.getPlayer().getUniqueID()) >= this.limit)
			event.setCanceled(true);
		
	}

	@Override
	public void afterTrade(PostTradeEvent event) {
		
		addTrade(event.getPlayer().getUniqueID());
		
	}

	private int getTradeCount(UUID playerID)
	{
		if(tradeHistory.containsKey(playerID))
			return tradeHistory.get(playerID);
		return 0;
	}
	
	private void addTrade(UUID playerID)
	{
		tradeHistory.put(playerID, getTradeCount(playerID) + 1);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		
		compound.putInt("Limit", this.limit);
		ListNBT memoryList = new ListNBT();
		this.tradeHistory.forEach((id,count) ->{
			CompoundNBT thisMemory = new CompoundNBT();
			thisMemory.putUniqueId("id", id);
			thisMemory.putInt("count", count);
			memoryList.add(thisMemory);
		});
		compound.put("Memory", memoryList);
		return compound;
	}

	@Override
	public void readNBT(CompoundNBT compound) {
		
		
		
	}
	
	private static class Deserializer implements ITradeRuleDeserializer<PlayerTradeLimit>
	{
		@Override
		public PlayerTradeLimit deserialize(CompoundNBT compound) {
			return null;
		}
	}

}
