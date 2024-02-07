package io.github.lightman314.lightmanscurrency.common.capability.spawn_tracker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.common.capability.CurrencyCapabilities;
import io.github.lightman314.lightmanscurrency.common.capability.spawn_tracker.ISpawnTracker;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SpawnTrackerCapability {

	@Deprecated
	public static LazyOptional<ISpawnTracker> getSpawnerTracker(@Nonnull final LivingEntity entity) {
		return entity.getCapability(CurrencyCapabilities.SPAWN_TRACKER);
	}

	@Nullable
	public static ISpawnTracker lazyGetSpawnerTracker(@Nonnull final LivingEntity entity) {
		LazyOptional<ISpawnTracker> optional = entity.getCapability(CurrencyCapabilities.SPAWN_TRACKER);
		if(optional.isPresent())
			return optional.orElseThrow(() -> new RuntimeException("Unexpected error occurred!"));
		return null;
	}
	
	public static ICapabilityProvider createProvider(final LivingEntity livingEntity)
	{
		return new Provider(livingEntity);
	}
	
	public static class SpawnTracker implements ISpawnTracker
	{
		
		final LivingEntity entity;
		MobSpawnType reason = MobSpawnType.NATURAL;
		
		public SpawnTracker(LivingEntity entity) {
			this.entity = entity;
		}

		@Override
		public MobSpawnType spawnReason() {
			return this.reason;
		}
		
		@Override
		public void setSpawnReason(MobSpawnType reason) {
			this.reason = reason;
		}

		@Override
		public CompoundTag save() {
			CompoundTag compound = new CompoundTag();
			compound.putString("SpawnReason", this.reason.toString());
			return compound;
		}

		@Override
		public void load(CompoundTag compound) {
			if(compound.contains("SpawnReason", Tag.TAG_STRING))
				this.reason = LootManager.deserializeSpawnReason(compound.getString("SpawnReason"));
		}
		
	}
	
	public static class Provider implements ICapabilitySerializable<Tag>{
		final LazyOptional<ISpawnTracker> optional;
		final ISpawnTracker handler;
		Provider(final LivingEntity entity)
		{
			this.handler = new SpawnTracker(entity);
			this.optional = LazyOptional.of(() -> this.handler);
		}
		
		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nullable Capability<T> capability, Direction facing) {
			return CurrencyCapabilities.SPAWN_TRACKER.orEmpty(capability, this.optional);
		}
		
		@Override
		public Tag serializeNBT() {
			return handler.save();
		}
		
		@Override
		public void deserializeNBT(Tag tag) {
			if(tag instanceof CompoundTag)
				handler.load((CompoundTag)tag);
		}
		
	}
	
}
