package io.github.lightman314.lightmanscurrency.common.capability;

import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.CompoundNBT;

public interface ISpawnTracker {

	public SpawnReason spawnReason();
	public void setSpawnReason(SpawnReason reason);
	
	public CompoundNBT save();
	public void load(CompoundNBT compound);
	
}
