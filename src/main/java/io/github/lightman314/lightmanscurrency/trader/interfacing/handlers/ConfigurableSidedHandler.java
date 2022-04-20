package io.github.lightman314.lightmanscurrency.trader.interfacing.handlers;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.trader.settings.directional.DirectionalSettings;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public abstract class ConfigurableSidedHandler<H> extends SidedHandler<H> {
	
	protected final DirectionalSettings inputSides;
	public DirectionalSettings getInputSides() { return this.inputSides; }
	protected final DirectionalSettings outputSides;
	public DirectionalSettings getOutputSides() { return this.outputSides; }
	
	protected static final String UPDATE_INPUT_SIDE = "inputSide";
	protected static final String UPDATE_OUTPUT_SIDE = "outputSide";
	
	protected ConfigurableSidedHandler() { this(ImmutableList.of()); }
	
	protected ConfigurableSidedHandler(ImmutableList<Direction> ignoreSides) {
		this.inputSides = new DirectionalSettings(ignoreSides);
		this.outputSides = new DirectionalSettings(ignoreSides);
	}
	
	public void toggleInputSide(@Nonnull Direction side) {
		
		this.inputSides.set(side, !this.inputSides.get(side));
		this.markDirty();
		if(this.isClient())
		{
			CompoundTag message = Settings.initUpdateInfo(UPDATE_INPUT_SIDE);
			message.putInt("side", side.get3DDataValue());
			message.putBoolean("newValue", this.inputSides.get(side));
			this.sendMessage(message);
		}
		
	}
	
	public void toggleOutputSide(Direction side) {
		
		this.outputSides.set(side, !this.outputSides.get(side));
		this.markDirty();
		if(this.isClient())
		{
			CompoundTag message = Settings.initUpdateInfo(UPDATE_OUTPUT_SIDE);
			message.putInt("side", side.get3DDataValue());
			message.putBoolean("newValue", this.outputSides.get(side));
			this.sendMessage(message);
		}
		
	}
	
	@Override
	public void receiveMessage(CompoundTag compound) {
		if(Settings.checkUpdateType(compound, UPDATE_INPUT_SIDE))
		{
			Direction side = Direction.from3DDataValue(compound.getInt("side"));
			if(compound.getBoolean("newValue") != this.inputSides.get(side))
				this.toggleInputSide(side);
		}
		else if(Settings.checkUpdateType(compound, UPDATE_OUTPUT_SIDE))
		{
			Direction side = Direction.from3DDataValue(compound.getInt("side"));
			if(compound.getBoolean("newValue") != this.outputSides.get(side))
				this.toggleOutputSide(side);
		}
		
	}
	
	@Override
	public final CompoundTag save() {
		CompoundTag compound = new CompoundTag();
		compound.put("InputSides", this.inputSides.save(new CompoundTag()));
		compound.put("OutputSides", this.outputSides.save(new CompoundTag()));
		this.saveAdditional(compound);
		return compound;
	}
	
	protected void saveAdditional(CompoundTag compound) { }

	@Override
	public void load(CompoundTag compound) {
		if(compound.contains("InputSides", Tag.TAG_COMPOUND))
			this.inputSides.load(compound.getCompound("InputSides"));
		if(compound.contains("OutputSides", Tag.TAG_COMPOUND))
			this.outputSides.load(compound.getCompound("OutputSides"));
	}

}
