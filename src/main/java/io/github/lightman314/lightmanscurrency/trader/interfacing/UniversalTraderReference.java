package io.github.lightman314.lightmanscurrency.trader.interfacing;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import net.minecraft.nbt.CompoundNBT;

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
	
	public CompoundNBT save()
	{
		CompoundNBT compound = new CompoundNBT();
		if(this.traderID != null)
			compound.putUniqueId("traderID", this.traderID);
		return compound;
	}
	
	public void load(CompoundNBT compound)
	{
		if(compound.contains("traderID"))
			this.traderID = compound.getUniqueId("traderID");
	}
	
	public boolean isClient() { return this.clientCheck.get(); }
	
	public UniversalTraderData getTrader() {
		if(this.traderID == null)
			return null;
		return this.isClient() ? ClientTradingOffice.getData(this.traderID) : TradingOffice.getData(this.traderID);
	}
	
}
