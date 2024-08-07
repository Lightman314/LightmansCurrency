package io.github.lightman314.lightmanscurrency.common.traderinterface;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;

public class NetworkTradeReference extends NetworkTraderReference{

	private final BiFunction<CompoundTag,HolderLookup.Provider,TradeData> tradeDeserializer;
	
	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	private TradeData tradeData = null;
	public boolean hasTrade() { return this.tradeIndex >= 0 && this.tradeData != null; }
	public TradeData getLocalTrade() { return this.tradeData; }
	
	public void setTrade(int tradeIndex) {
		this.tradeIndex = tradeIndex;
		if(tradeIndex < 0)
		{
			this.tradeData = null;
			return;
		}
		this.tradeData = copyTrade(this.getTrueTrade());
		if(this.tradeData == null)
		{
			LightmansCurrency.LogWarning("Trade index of '" + this.tradeIndex + "' does not result in a valid trade on the " + DebugUtil.getSideText(this.isClient()) + ". Resetting back to no trade selected.");
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
		return this.tradeDeserializer.apply(trade.getAsNBT(this.registryAccess()),this.registryAccess());
	}
	
	public NetworkTradeReference(@Nonnull Supplier<Boolean> clientCheck, @Nonnull Supplier<HolderLookup.Provider> lookupSource, @Nonnull BiFunction<CompoundTag, HolderLookup.Provider,TradeData> tradeDeserializer) {
		super(clientCheck,lookupSource);
		this.tradeDeserializer = tradeDeserializer;
	}
	
	public TradeData getTrueTrade() {
		if(this.tradeIndex < 0)
			return null;
		TraderData trader = this.getTrader();
		if(trader != null)
			return trader.getTrade(this.tradeIndex);
		return null;
	}
	
	public CompoundTag save(@Nonnull HolderLookup.Provider lookup) {
		CompoundTag compound = super.save(lookup);
		if(this.tradeData != null && this.tradeIndex >= 0)
		{
			compound.putInt("TradeIndex", this.tradeIndex);
			compound.put("Trade", this.tradeData.getAsNBT(lookup));
		}
		return compound;
	}
	
	public void load(CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
		super.load(compound,lookup);
		//Load old trade index saveItem
		if(compound.contains("tradeIndex", Tag.TAG_INT))
			this.tradeIndex = compound.getInt("tradeIndex");
		//Load new trade index saveItem
		if(compound.contains("TradeIndex", Tag.TAG_INT))
			this.tradeIndex = compound.getInt("TradeIndex");
		//Load old trade saveItem
		if(compound.contains("trade", Tag.TAG_COMPOUND))
			this.tradeData = this.tradeDeserializer.apply(compound.getCompound("trade"),lookup);
		//Load new trade saveItem
		else if(compound.contains("Trade", Tag.TAG_COMPOUND))
			this.tradeData = this.tradeDeserializer.apply(compound.getCompound("Trade"),lookup);
	}
	
	
}
