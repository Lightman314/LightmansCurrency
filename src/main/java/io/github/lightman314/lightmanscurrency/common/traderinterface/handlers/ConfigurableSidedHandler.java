package io.github.lightman314.lightmanscurrency.common.traderinterface.handlers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
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
			CompoundTag message = initUpdateInfo(UPDATE_INPUT_SIDE);
			message.putInt("side", side.get3DDataValue());
			message.putBoolean("newValue", this.inputSides.get(side));
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
	
	public void toggleOutputSide(Direction side) {
		
		this.outputSides.set(side, !this.outputSides.get(side));
		this.markDirty();
		if(this.isClient())
		{
			CompoundTag message = initUpdateInfo(UPDATE_OUTPUT_SIDE);
			message.putInt("side", side.get3DDataValue());
			message.putBoolean("newValue", this.outputSides.get(side));
			this.sendMessage(message);
		}
		
	}
	
	@Override
	public void receiveMessage(CompoundTag compound) {
		if(isUpdateType(compound, UPDATE_INPUT_SIDE))
		{
			Direction side = Direction.from3DDataValue(compound.getInt("side"));
			if(compound.getBoolean("newValue") != this.inputSides.get(side))
				this.toggleInputSide(side);
		}
		else if(isUpdateType(compound, UPDATE_OUTPUT_SIDE))
		{
			Direction side = Direction.from3DDataValue(compound.getInt("side"));
			if(compound.getBoolean("newValue") != this.outputSides.get(side))
				this.toggleOutputSide(side);
		}
		
	}
	
	@Override
	public final CompoundTag save(@Nonnull HolderLookup.Provider lookup) {
		CompoundTag compound = new CompoundTag();
		compound.put("InputSides", this.inputSides.save(new CompoundTag()));
		compound.put("OutputSides", this.outputSides.save(new CompoundTag()));
		this.saveAdditional(compound, lookup);
		return compound;
	}
	
	protected void saveAdditional(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) { }

	@Override
	public void load(CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		if(compound.contains("InputSides", Tag.TAG_COMPOUND))
			this.inputSides.load(compound.getCompound("InputSides"));
		if(compound.contains("OutputSides", Tag.TAG_COMPOUND))
			this.outputSides.load(compound.getCompound("OutputSides"));
	}
	
	public static class DirectionalSettings {

		public final ImmutableList<Direction> ignoreSides;
		private final Map<Direction,Boolean> sideValues = new HashMap<>();
		
		public DirectionalSettings() { this(ImmutableList.of()); }
		
		public DirectionalSettings(ImmutableList<Direction> ignoreSides)
		{
			this.ignoreSides = ignoreSides;
		}
		
		public boolean allows(Direction side) { return !this.ignoreSides.contains(side); }
		
		public boolean get(Direction side) {
			if(this.ignoreSides.contains(side))
				return false;
			return this.sideValues.getOrDefault(side, false);
		}
		
		public void set(Direction side, boolean value) {
			if(this.ignoreSides.contains(side))
				return;
			this.sideValues.put(side, value);
		}
		
		public CompoundTag save(CompoundTag compound)
		{
			for(Direction side : Direction.values())
			{
				if(this.ignoreSides.contains(side))
					continue;
				compound.putBoolean(side.toString(), this.get(side));
			}
			return compound;
		}
		
		public void load(CompoundTag compound)
		{
			this.sideValues.clear();
			for(Direction side : Direction.values())
			{
				if(this.ignoreSides.contains(side))
					continue;
				if(compound.contains(side.toString()))
					this.set(side, compound.getBoolean(side.toString()));
			}
		}
		
	}

}
