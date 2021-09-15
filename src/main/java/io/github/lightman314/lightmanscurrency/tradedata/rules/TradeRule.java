package io.github.lightman314.lightmanscurrency.tradedata.rules;

import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public abstract class TradeRule {
	
	private final ResourceLocation type;
	
	public void beforeTrade(PreTradeEvent event) {}
	public void afterTrade(PostTradeEvent event) {}
	
	protected TradeRule(ResourceLocation type)
	{
		this.type = type;
	}
	
	public CompoundNBT getNBT()
	{
		CompoundNBT compound = new CompoundNBT();
		compound.putString("type", this.type.toString());
		return write(compound);
	}
	
	protected abstract CompoundNBT write(CompoundNBT compound);
	
	public abstract void readNBT(CompoundNBT compound);
	
}
