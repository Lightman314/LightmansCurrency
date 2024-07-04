package io.github.lightman314.lightmanscurrency.common.traderinterface.handlers;

import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public abstract class SidedHandler<H>{
	
	private TraderInterfaceBlockEntity parent;
	public TraderInterfaceBlockEntity getParent() { return this.parent; }
	public void setParent(TraderInterfaceBlockEntity parent) { if(this.parent == null) this.parent = parent; }
	
	public abstract H getHandler(Direction side);
	
	public abstract ResourceLocation getType();
	public abstract String getTag();
	
	public abstract CompoundTag save(@Nonnull HolderLookup.Provider lookup);
	public abstract void load(CompoundTag compound, @Nonnull HolderLookup.Provider lookup);
	
	public void sendMessage(CompoundTag message) {
		this.parent.sendHandlerMessage(this.getType(), message);
	}
	
	protected final boolean isClient() { return this.parent.isClient(); }
	
	public final void markDirty() { this.parent.setHandlerDirty(this); }
	
	public abstract void receiveMessage(CompoundTag message);
	
}
