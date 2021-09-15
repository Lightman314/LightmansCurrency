package io.github.lightman314.lightmanscurrency.tradedata.memory;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.tradedata.memory.TradeActionEvents.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.tradedata.memory.TradeActionEvents.PreTradeEvent;
import net.minecraft.nbt.CompoundNBT;

public interface ITradeMemory<T extends TradeData<?>> {

	static final Map<String,TradeData<?>> registeredDeserializers = new HashMap<>();
	
	public String getID();
	
	public void beforeTrade(PreTradeEvent<T> event);
	public void afterTrade(PostTradeEvent<T> event);
	
	public CompoundNBT writeNBT();
	public void readNBT(CompoundNBT nbt);
	
}
