package io.github.lightman314.lightmanscurrency.tradedata;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.tradedata.memory.ITradeMemory;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinValue;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;

public abstract class TradeData<T extends TradeData<?>> {

	public static final String DEFAULT_KEY = "Trades";
	
	protected CoinValue cost = new CoinValue();
	protected boolean isFree = false;
	
	List<ITradeMemory<T>> memories = new ArrayList<>();
	
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
	}
	
	public boolean hasEnoughMoney(CoinValue coinStorage)
	{
		return tradesPossibleWithStoredMoney(coinStorage) > 0;
	}
	
	public long tradesPossibleWithStoredMoney(CoinValue coinStorage)
	{
		if(this.isFree)
			return 1;
		if(this.cost.getRawValue() == 0) //To avoid dividing by 0
			return 0;
		long coinValue = coinStorage.getRawValue();
		long price = this.cost.getRawValue();
		return coinValue / price;
	}
	
}
