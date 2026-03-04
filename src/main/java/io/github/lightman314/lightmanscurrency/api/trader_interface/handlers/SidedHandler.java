package io.github.lightman314.lightmanscurrency.api.trader_interface.handlers;

import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public abstract class SidedHandler<H> implements LazyPacketData.IBuilderProvider {
	
	private TraderInterfaceBlockEntity<?> parent;
	public TraderInterfaceBlockEntity<?> getParent() { return this.parent; }
	public void setParent(TraderInterfaceBlockEntity<?> parent) { if(this.parent == null) this.parent = parent; }

    @Override
    public LazyPacketData.Builder builder() { return LazyPacketData.builder(this.parent.registryAccess()); }

    public abstract H getHandler(Direction side);
	
	public abstract ResourceLocation getType();
	public abstract String getTag();
	
	public abstract CompoundTag save(DataContext<Tag> context);
	public abstract void load(CompoundTag compound,DataContext<Tag> context);

    public void setChanged(Consumer<LazyPacketData.Builder> dataWriter) { this.parent.setChanged(builder -> builder.modifyMap(this.getType().toString(),dataWriter)); }
	
	protected final boolean isClient() { return this.parent.isClient(); }
	
	public final void markDirty() { this.parent.setHandlerDirty(this); }

    public abstract void handleSyncPacket(LazyPacketData data);
	
}
