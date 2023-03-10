package io.github.lightman314.lightmanscurrency.common.data_updating.events;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

public class ConvertUniversalTraderEvent extends Event {

	private TraderData trader = null;
	public TraderData getTrader() { return this.trader; }
	public void setTrader(TraderData trader) { this.trader = trader; }
	
	public final ResourceLocation type;
	public final CompoundNBT compound;

	public ConvertUniversalTraderEvent(CompoundNBT compound) {
		this.compound = compound;
		if(compound.contains("type"))
			this.type = new ResourceLocation(compound.getString("type"));
		else
			this.type = new ResourceLocation(LightmansCurrency.MODID,"null");
	}
	
}