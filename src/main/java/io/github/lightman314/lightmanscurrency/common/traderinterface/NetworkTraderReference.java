package io.github.lightman314.lightmanscurrency.common.traderinterface;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public class NetworkTraderReference implements IClientTracker {

	private final Supplier<Boolean> clientCheck;
	private final Supplier<HolderLookup.Provider> lookupSource;
	protected final HolderLookup.Provider registryAccess() { return this.lookupSource.get(); }
	long traderID = -1;
	public long getTraderID() { return this.traderID; }
	public boolean hasTrader() { return this.getTrader() != null; }
	public void setTrader(long traderID) {
		this.traderID = traderID; 
		if(this.getTrader() == null)
			this.traderID = -1;
	}
	
	public NetworkTraderReference(@Nonnull Supplier<Boolean> clientCheck,@Nonnull Supplier<HolderLookup.Provider> lookupSource)
	{
		this.clientCheck = clientCheck;
		this.lookupSource = lookupSource;
	}
	
	public CompoundTag save(@Nonnull HolderLookup.Provider lookup)
	{
		CompoundTag compound = new CompoundTag();
		compound.putLong("TraderID", this.traderID);
		return compound;
	}
	
	public void load(CompoundTag compound, @Nonnull HolderLookup.Provider lookup)
	{
		if(compound.contains("TraderID"))
			this.traderID = compound.getLong("TraderID");
	}
	
	public boolean isClient() { return this.clientCheck.get(); }
	
	public TraderData getTrader() {
		if(this.traderID < 0)
			return null;
		TraderData trader = TraderSaveData.GetTrader(this.isClient(), this.traderID);
		return trader == null || !trader.showOnTerminal() ? null : trader;
	}
	
}
