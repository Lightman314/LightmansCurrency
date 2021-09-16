package io.github.lightman314.lightmanscurrency.tradedata.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

public class PlayerTradeLimit extends TradeRule{
	
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "tradelimit");
	public static final ITradeRuleDeserializer<PlayerTradeLimit> DESERIALIZER = new Deserializer();
	
	private int limit = 1;
	public int getLimit() { return this.limit; }
	public void setLimit(int newLimit) { this.limit = newLimit; }
	
	Map<UUID,Integer> tradeHistory = new HashMap<>();
	public void resetMemory() { this.tradeHistory.clear(); }
	
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
		
		if(compound.contains("Limit", Constants.NBT.TAG_INT))
			this.limit = compound.getInt("Limit");
		if(compound.contains("Memory", Constants.NBT.TAG_LIST))
		{
			this.tradeHistory.clear();
			ListNBT memoryList = compound.getList("Memory", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < memoryList.size(); i++)
			{
				CompoundNBT thisMemory = memoryList.getCompound(i);
				UUID id = null;
				int count = 0;
				if(thisMemory.contains("id"))
					id = thisMemory.getUniqueId("id");
				if(thisMemory.contains("count", Constants.NBT.TAG_INT))
					count = thisMemory.getInt("count");
				if(id != null)
					this.tradeHistory.put(id, count);
			}
		}
		
	}
	
	@Override
	public int getGUIX() { return 48; }
	
	private static class Deserializer implements ITradeRuleDeserializer<PlayerTradeLimit>
	{
		@Override
		public PlayerTradeLimit deserialize(CompoundNBT compound) {
			return null;
		}
	}

	@Override
	public void initTab(TradeRuleScreen screen) {
		
		
	}
	
	@Override
	public void renderTab(TradeRuleScreen screen, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		
		
	}
	@Override
	public void onTabClose(TradeRuleScreen screen) {
		
		
	}

}
