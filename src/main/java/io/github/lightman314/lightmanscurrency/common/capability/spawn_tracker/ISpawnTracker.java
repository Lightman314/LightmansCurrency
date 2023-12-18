package io.github.lightman314.lightmanscurrency.common.capability.spawn_tracker;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.MobSpawnType;

public interface ISpawnTracker {

	MobSpawnType spawnReason();
	void setSpawnReason(MobSpawnType reason);
	
	CompoundTag save();
	void load(CompoundTag compound);
	
}
