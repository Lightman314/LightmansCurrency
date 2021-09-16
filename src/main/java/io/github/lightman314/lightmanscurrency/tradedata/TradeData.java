package io.github.lightman314.lightmanscurrency.tradedata;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.tradedata.rules.ITradeRuleHandler;
import io.github.lightman314.lightmanscurrency.tradedata.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;

public abstract class TradeData implements ITradeRuleHandler {

	public static final String DEFAULT_KEY = "Trades";
	
	protected CoinValue cost = new CoinValue();
	protected boolean isFree = false;
	
	List<TradeRule> rules = new ArrayList<>();
	
	public boolean isFree()
	{
		return this.isFree && cost.getRawValue() <= 0;
	}
	
	public void setFree(boolean isFree)
	{
		this.isFree = isFree;
		LightmansCurrency.LogInfo("Set free state of a trade to " + isFree);
	}
	
	public CoinValue getCost()
	{
		return this.cost;
	}
	
	public void setCost(CoinValue value)
	{
		this.cost = value;
	}
	
	public CompoundNBT getAsNBT()
	{
		CompoundNBT tradeNBT = new CompoundNBT();
		this.cost.writeToNBT(tradeNBT,"Price");
		tradeNBT.putBoolean("IsFree", this.isFree);
		TradeRule.writeRules(tradeNBT, this.rules);
		
		return tradeNBT;
	}
	
	protected void loadFromNBT(CompoundNBT nbt)
	{
		if(nbt.contains("Price", Constants.NBT.TAG_INT))
			cost.readFromOldValue(nbt.getInt("Price"));
		else if(nbt.contains("Price", Constants.NBT.TAG_LIST))
			cost.readFromNBT(nbt, "Price");
		//Set whether it's free or not
		if(nbt.contains("IsFree"))
			this.isFree = nbt.getBoolean("IsFree");
		else
			this.isFree = false;
		
		this.rules.clear();
		this.rules = TradeRule.readRules(nbt);
		
	}
	
	public boolean hasEnoughMoney(CoinValue coinStorage)
	{
		return tradesPossibleWithStoredMoney(coinStorage) > 0;
	}
	
	public long tradesPossibleWithStoredMoney(CoinValue coinStorage)
	{
		if(this.isFree)
			return 1;
		if(this.cost.getRawValue() == 0)
			return 0;
		long coinValue = coinStorage.getRawValue();
		long price = this.cost.getRawValue();
		return coinValue / price;
	}
	
	@Override
	public void beforeTrade(PreTradeEvent event) {
		this.rules.forEach(rule -> rule.beforeTrade(event));
	}

	@Override
	public void afterTrade(PostTradeEvent event) {
		this.rules.forEach(rule -> rule.afterTrade(event));
	}
	
	public void addRule(TradeRule newRule)
	{
		if(newRule == null)
			return;
		//Confirm a lack of duplicate rules
		for(int i = 0; i < this.rules.size(); i++)
		{
			if(newRule.type == this.rules.get(i).type)
				return;
		}
		this.rules.add(newRule);
	}
	
	public List<TradeRule> getRules() { return this.rules; }
	
	public void setRules(List<TradeRule> rules) { this.rules = rules; }
	
	public void removeRule(TradeRule rule)
	{
		if(this.rules.contains(rule))
			this.rules.remove(rule);
	}
	
	public void clearRules()
	{
		this.rules.clear();
	}
	
}
