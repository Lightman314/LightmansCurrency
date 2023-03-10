package io.github.lightman314.lightmanscurrency.common.traderinterface.handlers;

import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public abstract class SidedHandler<H>{
	
	private TraderInterfaceBlockEntity parent;
	public TraderInterfaceBlockEntity getParent() { return this.parent; }
	public void setParent(TraderInterfaceBlockEntity parent) { if(this.parent == null) this.parent = parent; }
	
	public abstract H getHandler(Direction side);
	
	public abstract ResourceLocation getType();
	public abstract String getTag();
	
	public abstract CompoundNBT save();
	public abstract void load(CompoundNBT compound);
	
	public void sendMessage(CompoundNBT message) {
		this.parent.sendHandlerMessage(this.getType(), message);
	}
	
	protected final boolean isClient() { return this.parent.isClient(); }
	
	public final void markDirty() { this.parent.setHandlerDirty(this); }
	
	public abstract void receiveMessage(CompoundNBT message);
	
}