package io.github.lightman314.lightmanscurrency.common.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.MobSpawnType;

public interface ISpawnTracker {

	public MobSpawnType spawnReason();
	public void setSpawnReason(MobSpawnType reason);
	
	public CompoundTag save();
	public void load(CompoundTag compound);
	
}
