package io.github.lightman314.lightmanscurrency.tradedata.memory;

import io.github.lightman314.lightmanscurrency.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.tradedata.memory.TradeActionEvents.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.tradedata.memory.TradeActionEvents.PreTradeEvent;
import net.minecraft.nbt.CompoundNBT;

public class PlayerTradeMemory<T extends TradeData<?>> implements ITradeMemory<T>{

	//Map<UUID,Integer> history = new HashMap<>();
	
	@Override
	public String getID() { return "playerTrade"; }
	
	@Override
	public void beforeTrade(PreTradeEvent<T> event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterTrade(PostTradeEvent<T> event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CompoundNBT writeNBT() {
		CompoundNBT compound = new CompoundNBT();
		
		return compound;
	}

	@Override
	public void readNBT(CompoundNBT nbt) {
		// TODO Auto-generated method stub
		
	}

}
