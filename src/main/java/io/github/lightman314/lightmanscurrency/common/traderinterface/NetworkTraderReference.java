package io.github.lightman314.lightmanscurrency.common.traderinterface;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.nbt.CompoundTag;

public class NetworkTraderReference {

	private final Supplier<Boolean> clientCheck;
	long traderID = -1;
	public long getTraderID() { return this.traderID; }
	public boolean hasTrader() { return this.getTrader() != null; }
	public void setTrader(long traderID) {
		this.traderID = traderID; 
		if(this.getTrader() == null)
			this.traderID = -1;
	}
	
	public NetworkTraderReference(Supplier<Boolean> clientCheck)
	{
		this.clientCheck = clientCheck;
	}
	
	public CompoundTag save()
	{
		CompoundTag compound = new CompoundTag();
		compound.putLong("TraderID", this.traderID);
		return compound;
	}
	
	public void load(CompoundTag compound)
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
