package io.github.lightman314.lightmanscurrency.trader.interfacing;

import java.util.function.Function;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.trader.ITradeSource;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;

public class UniversalTradeReference<T extends TradeData> extends UniversalTraderReference{

	private Function<CompoundNBT,T> tradeDeserializer;
	
	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	private T tradeData = null;
	private Class<?> tradeClass = null;
	public boolean hasTrade() { return this.tradeIndex >= 0 && this.tradeData != null; }
	public T getLocalTrade() { return this.tradeData; }
	
	public void setTrade(int tradeIndex) {
		this.tradeIndex = tradeIndex;
		T trade = this.getTrueTrade();
		if(trade != null)
		{
			this.tradeData = trade;
			this.tradeClass = trade.getClass();
		}
		else
			this.tradeIndex = -1;
	}
	
	public void refreshTrade() {
		if(!this.hasTrade())
			return;
		T newTrade = this.getTrueTrade();
		if(newTrade != null)
		{
			this.tradeData = newTrade;
			this.tradeClass = newTrade.getClass();
		}
	}
	
	public UniversalTradeReference(Supplier<Boolean> clientCheck, Function<CompoundNBT,T> tradeDeserializer) {
		super(clientCheck);
		this.tradeDeserializer = tradeDeserializer;
	}
	
	@SuppressWarnings("unchecked")
	public T getTrueTrade() {
		if(this.tradeIndex < 0)
			return null;
		UniversalTraderData trader = this.getTrader();
		if(trader instanceof ITradeSource<?>)
		{
			ITradeSource<?> tradeSource = (ITradeSource<?>)trader;
			Object trade = tradeSource.getTrade(this.tradeIndex);
			if(trade != null && trade.getClass() == this.tradeClass)
				return (T)trade;
		}
		return null;
	}
	
	public CompoundNBT save() {
		CompoundNBT compound = super.save();
		if(this.tradeData != null && this.tradeIndex >= 0)
		{
			compound.putInt("tradeIndex", this.tradeIndex);
			compound.put("trade", this.tradeData.getAsNBT());
		}
		return compound;
	}
	
	public void load(CompoundNBT compound) {
		super.load(compound);
		if(compound.hasUniqueId("tradeIndex"))
			this.tradeIndex = compound.getInt("tradeIndex");
		if(compound.contains("trade", Constants.NBT.TAG_COMPOUND))
		{
			this.tradeData = this.tradeDeserializer.apply(compound.getCompound("trade"));
			if(this.tradeData != null)
				this.tradeClass = this.tradeData.getClass();
		}
	}
	
	
}
