package io.github.lightman314.lightmanscurrency.trader.interfacing;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.nbt.CompoundTag;

public class UniversalTraderReference {

	private final Supplier<Boolean> clientCheck;
	UUID traderID = null;
	public boolean hasTrader() { return this.traderID != null; }
	public void setTrader(UUID traderID) {
		this.traderID = traderID; 
		if(this.getTrader() == null)
			this.traderID = null;
	}
	
	public UniversalTraderReference(Supplier<Boolean> clientCheck)
	{
		this.clientCheck = clientCheck;
	}
	
	public CompoundTag save()
	{
		CompoundTag compound = new CompoundTag();
		if(this.traderID != null)
			compound.putUUID("traderID", this.traderID);
		return compound;
	}
	
	public void load(CompoundTag compound)
	{
		if(compound.contains("traderID"))
			this.traderID = compound.getUUID("traderID");
	}
	
	public boolean isClient() { return this.clientCheck.get(); }
	
	public UniversalTraderData getTrader() {
		if(this.traderID == null)
			return null;
		return this.isClient() ? ClientTradingOffice.getData(this.traderID) : TradingOffice.getData(this.traderID);
	}
	
}
