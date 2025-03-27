package io.github.lightman314.lightmanscurrency.common.traderinterface.handlers;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.IDirectionalSettingsObject;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ConfigurableSidedHandler<H> extends SidedHandler<H> implements IDirectionalSettingsObject {


	protected final DirectionalSettings directionalSettings = new DirectionalSettings(this);
	
	protected static final String UPDATE_SIDE = "updateSide";

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
		TraderInterfaceBlockEntity be = this.getParent();
		return be == null ? null : be.getBlockState().getBlock();
	}

	public void toggleSide(Direction side, DirectionalSettingsState newState) {
		if(this.getSidedState(side) == newState)
			return;
		this.directionalSettings.setState(side,newState);
		this.markDirty();
		if(this.isClient())
		{
			CompoundTag message = initUpdateInfo(UPDATE_SIDE);
			message.putInt("side",side.get3DDataValue());
			message.putString("newValue",newState.toString());
			this.sendMessage(message);
		}
	}
	
	public static CompoundTag initUpdateInfo(String updateType)
	{
		CompoundTag compound = new CompoundTag();
		compound.putString("UpdateType", updateType);
		return compound;
	}
	
	public static boolean isUpdateType(CompoundTag updateInfo, String updateType)
	{
		if(updateInfo.contains("UpdateType",Tag.TAG_STRING))
			return updateInfo.getString("UpdateType").contentEquals(updateType);
		return false;
	}
	
	@Override
	public void receiveMessage(CompoundTag compound) {
		if(isUpdateType(compound, UPDATE_SIDE))
		{
			Direction side = Direction.from3DDataValue(compound.getInt("side"));
			DirectionalSettingsState state = DirectionalSettingsState.parse(compound.getString("newValue"));
			if(state != this.getSidedState(side))
				this.toggleSide(side,state);
		}
	}
	
	@Override
	public final CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		this.directionalSettings.save(compound,"InputOutputSides");
		this.saveAdditional(compound);
		return compound;
	}
	
	protected void saveAdditional(CompoundTag compound) { }

	@Override
	public void load(CompoundTag compound) {
		this.directionalSettings.load(compound,"InputOutputSides");
		if(compound.contains("InputSides",Tag.TAG_COMPOUND))
		{
			//Load old input states
			CompoundTag entry = compound.getCompound("InputSides");
			for(Direction side : Direction.values())
			{
				if(this.ignoreSides.contains(side))
					continue;
				if(compound.contains(side.toString()) && compound.getBoolean(side.toString()))
					this.directionalSettings.setState(side,DirectionalSettingsState.INPUT);
			}
		}
		if(compound.contains("OutputSides",Tag.TAG_COMPOUND))
		{
			//Load old output states
			CompoundTag entry = compound.getCompound("OutputSides");
			for(Direction side : Direction.values())
			{
				if(this.ignoreSides.contains(side))
					continue;
				if(compound.contains(side.toString()) && compound.getBoolean(side.toString()))
				{
					DirectionalSettingsState state = this.directionalSettings.getState(side);
					if(state.allowsInputs())
						this.directionalSettings.setState(side,DirectionalSettingsState.INPUT_AND_OUTPUT);
					else
						this.directionalSettings.setState(side,DirectionalSettingsState.OUTPUT);
				}
			}
		}
	}

}
