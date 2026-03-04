package io.github.lightman314.lightmanscurrency.api.trader_interface.handlers;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.IDirectionalSettingsObject;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;

public abstract class ConfigurableSidedHandler<H> extends SidedHandler<H> implements IDirectionalSettingsObject {

	protected final DirectionalSettings directionalSettings = new DirectionalSettings(this);

	private final ImmutableList<Direction> ignoreSides;

	protected ConfigurableSidedHandler() { this(ImmutableList.of()); }

	protected ConfigurableSidedHandler(ImmutableList<Direction> ignoreSides) {
		this.ignoreSides = ignoreSides;
	}

	@Override
	public List<Direction> getIgnoredSides() { return this.ignoreSides; }

	@Override
	public DirectionalSettingsState getSidedState(Direction side) { return this.directionalSettings.getState(side); }

	@Nullable
	@Override
	public Block getDisplayBlock() {
		TraderInterfaceBlockEntity<?> be = this.getParent();
		return be == null ? null : be.getBlockState().getBlock();
	}

	public void toggleSide(Direction side,DirectionalSettingsState newState) {
		if(this.getSidedState(side) == newState)
			return;
		this.directionalSettings.setState(side,newState);
		this.markDirty();
		if(this.isClient())
		{
            this.setChanged(builder -> builder.addToList("SideUpdates",this.builder()
                    .setInt("side",side.get3DDataValue())
                    .setInt("newValue",this.directionalSettings.getState(side).ordinal()),
                    LazyPacketData.BUILDER_FACTORY));
		}
	}

    @Override
    public void handleSyncPacket(LazyPacketData data) {
        if(data.contains("SideUpdates"))
        {
            for(LazyPacketData entry : data.getList("SideUpdates",LazyPacketData.class))
            {
                Direction side = Direction.from3DDataValue(entry.getInt("side"));
                DirectionalSettingsState state = DirectionalSettingsState.parse(entry.getString("newValue"));
                if(state != this.getSidedState(side))
                    this.toggleSide(side,state);
            }
        }
    }

    @Override
	public final CompoundTag save(DataContext<Tag> context) {
		CompoundTag compound = new CompoundTag();
        compound.put("sideData",context.write(this.directionalSettings,DirectionalSettings.CODEC));
		return compound;
	}
	
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) { }

	@Override
	public void load(CompoundTag compound,DataContext<Tag> context) {
		this.directionalSettings.loadOldData(compound,"InputOutputSides");
        if(compound.contains("sideData"))
            this.directionalSettings.copy(context.read(compound.get("sideData"),DirectionalSettings.CODEC));
	}

}
