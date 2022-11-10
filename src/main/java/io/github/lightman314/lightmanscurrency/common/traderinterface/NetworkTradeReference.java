package io.github.lightman314.lightmanscurrency.common.traderinterface;

import java.util.function.Function;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.ITradeSource;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class NetworkTradeReference extends NetworkTraderReference{

	private final Function<CompoundTag,TradeData> tradeDeserializer;
	
	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	private TradeData tradeData = null;
	public boolean hasTrade() { return this.tradeIndex >= 0 && this.tradeData != null; }
	public TradeData getLocalTrade() { return this.tradeData; }
	
	public void setTrade(int tradeIndex) {
		this.tradeIndex = tradeIndex;
		this.tradeData = copyTrade(this.getTrueTrade());
		if(this.tradeData == null)
		{
			LightmansCurrency.LogWarning("Trade index of '" + this.tradeIndex + "' does not result in a valid trade. Resetting back to no trade selected.");
			this.tradeIndex = -1;
		}
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
	
	public NetworkTradeReference(Supplier<Boolean> clientCheck, Function<CompoundTag,TradeData> tradeDeserializer) {
		super(clientCheck);
		this.tradeDeserializer = tradeDeserializer;
	}
	
	public TradeData getTrueTrade() {
		if(this.tradeIndex < 0)
			return null;
		TraderData trader = this.getTrader();
		if(trader instanceof ITradeSource<?> tradeSource)
			return tradeSource.getTrade(this.tradeIndex);
		return null;
	}
	
	public CompoundTag save() {
		CompoundTag compound = super.save();
		if(this.tradeData != null && this.tradeIndex >= 0)
		{
			compound.putInt("TradeIndex", this.tradeIndex);
			compound.put("Trade", this.tradeData.getAsNBT());
		}
		return compound;
	}
	
	public void load(CompoundTag compound) {
		super.load(compound);
		//Load old trade index save
		if(compound.contains("tradeIndex", Tag.TAG_INT))
			this.tradeIndex = compound.getInt("tradeIndex");
		//Load new trade index save
		if(compound.contains("TradeIndex", Tag.TAG_INT))
			this.tradeIndex = compound.getInt("TradeIndex");
		//Load old trade save
		if(compound.contains("trade", Tag.TAG_COMPOUND))
			this.tradeData = this.tradeDeserializer.apply(compound.getCompound("trade"));
		//Load new trade save
		else if(compound.contains("Trade", Tag.TAG_COMPOUND))
			this.tradeData = this.tradeDeserializer.apply(compound.getCompound("Trade"));
	}
	
	
}