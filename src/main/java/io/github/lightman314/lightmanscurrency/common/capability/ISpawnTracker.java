package io.github.lightman314.lightmanscurrency.common.capability;

import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.CompoundNBT;

public interface ISpawnTracker {

	SpawnReason spawnReason();
	void setSpawnReason(SpawnReason reason);
	
	CompoundNBT save();
	void load(CompoundNBT compound);
	
}
