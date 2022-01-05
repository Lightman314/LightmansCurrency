package io.github.lightman314.lightmanscurrency.trader.settings.directional;

import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public class DirectionalSettings {

	private final ImmutableList<Direction> ignoreSides;
	private final Map<Direction,Boolean> sideValues = Maps.newHashMap();
	
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
	
	public CompoundNBT save(CompoundNBT compound)
	{
		for(Direction side : Direction.values())
		{
			if(this.ignoreSides.contains(side))
				continue;
			compound.putBoolean(side.toString(), this.get(side));
		}
		return compound;
	}
	
	public void load(CompoundNBT compound)
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
