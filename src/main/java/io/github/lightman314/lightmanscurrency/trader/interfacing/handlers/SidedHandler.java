package io.github.lightman314.lightmanscurrency.trader.interfacing.handlers;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.trader.interfacing.TradeInteraction;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public abstract class SidedHandler<I extends TradeInteraction<I,?>,H>{
	
	private UniversalTraderInterfaceBlockEntity<I,?> parent;
	public UniversalTraderInterfaceBlockEntity<I,?> getParent() { return this.parent; }
	public void setParent(UniversalTraderInterfaceBlockEntity<I,?> parent) { if(this.parent == null) this.parent = parent; }
	
	public abstract H getHandler(Direction side);
	
	public abstract ResourceLocation getType();
	public abstract String getTag();
	
	public abstract CompoundTag save();
	public abstract void load(CompoundTag compound);
	
	public void sendMessage(CompoundTag message) {
		this.parent.sendHandlerMessage(this.getType(), message);
	}
	
	protected final boolean isClient() { return this.parent.isClient(); }
	
	public abstract void receiveMessage(CompoundTag message);
	
}
