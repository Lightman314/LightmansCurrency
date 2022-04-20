package io.github.lightman314.lightmanscurrency.trader.interfacing;

import java.util.function.Function;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.trader.ITradeSource;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class UniversalTradeReference extends UniversalTraderReference{

	private Function<CompoundTag,TradeData> tradeDeserializer;
	
	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	private TradeData tradeData = null;
	public boolean hasTrade() { return this.tradeIndex >= 0 && this.tradeData != null; }
	public TradeData getLocalTrade() { return this.tradeData; }
	
	public void setTrade(int tradeIndex) {
		this.tradeIndex = tradeIndex;
		this.tradeData = copyTrade(this.getTrueTrade());
		if(this.tradeData == null)
			this.tradeIndex = -1;
	}
	
	public void refreshTrade() {
		if(!this.hasTrade())
			return;
		TradeData newTrade = copyTrade(this.getTrueTrade());
		if(newTrade != null)
			this.tradeData = newTrade;
	}
	
	public TradeData copyTrade(TradeData trade) {
		if(trade == null)
			return null;
		return this.tradeDeserializer.apply(trade.getAsNBT());
	}
	
	public UniversalTradeReference(Supplier<Boolean> clientCheck, Function<CompoundTag,TradeData> tradeDeserializer) {
		super(clientCheck);
		this.tradeDeserializer = tradeDeserializer;
	}
	
	public TradeData getTrueTrade() {
		if(this.tradeIndex < 0)
			return null;
		UniversalTraderData trader = this.getTrader();
		if(trader instanceof ITradeSource<?>)
		{
			ITradeSource<?> tradeSource = (ITradeSource<?>)trader;
			return tradeSource.getTrade(this.tradeIndex);
		}
		return null;
	}
	
	public CompoundTag save() {
		CompoundTag compound = super.save();
		if(this.tradeData != null && this.tradeIndex >= 0)
		{
			compound.putInt("tradeIndex", this.tradeIndex);
			compound.put("trade", this.tradeData.getAsNBT());
		}
		return compound;
	}
	
	public void load(CompoundTag compound) {
		super.load(compound);
		if(compound.contains("tradeIndex", Tag.TAG_INT))
			this.tradeIndex = compound.getInt("tradeIndex");
		if(compound.contains("trade", Tag.TAG_COMPOUND))
			this.tradeData = this.tradeDeserializer.apply(compound.getCompound("trade"));
	}
	
	
}
