package io.github.lightman314.lightmanscurrency.common.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.core.LootManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

public class SpawnTrackerCapability {

	public static void register() {
		CapabilityManager.INSTANCE.register(ISpawnTracker.class,  new Capability.IStorage<ISpawnTracker>() {

			@Override
			public INBT writeNBT(Capability<ISpawnTracker> capability, ISpawnTracker instance, Direction side) {
				return instance.save();
			}

			@Override
			public void readNBT(Capability<ISpawnTracker> capability, ISpawnTracker instance, Direction side, INBT nbt) {
				if(nbt instanceof CompoundNBT)
				{
					instance.load((CompoundNBT)nbt);
				}
			}
		}, SpawnTracker::new);
	}
	
	public static LazyOptional<ISpawnTracker> getSpawnerTracker(@Nonnull final LivingEntity entity) {
		return entity.getCapability(CurrencyCapabilities.SPAWN_TRACKER);
	}
	
	public static ICapabilityProvider createProvider(final LivingEntity livingEntity)
	{
		return new Provider(livingEntity);
	}
	
	public static class SpawnTracker implements ISpawnTracker
	{
		
		final LivingEntity entity;
		SpawnReason reason = SpawnReason.NATURAL;
		
		public SpawnTracker() {
			this(null);
		}
		
		public SpawnTracker(LivingEntity entity) {
			this.entity = entity;
		}

		@Override
		public SpawnReason spawnReason() {
			return this.reason;
		}
		
		@Override
		public void setSpawnReason(SpawnReason reason) {
			this.reason = reason;
		}

		@Override
		public CompoundNBT save() {
			CompoundNBT compound = new CompoundNBT();
			compound.putString("SpawnReason", this.reason.toString());
			return compound;
		}

		@Override
		public void load(CompoundNBT compound) {
			if(compound.contains("SpawnReason", Constants.NBT.TAG_STRING))
				this.reason = LootManager.deserializeSpawnReason(compound.getString("SpawnReason"));
		}
		
	}
	
	public static class Provider implements ICapabilitySerializable<INBT>{
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
		public INBT serializeNBT() {
			return CurrencyCapabilities.SPAWN_TRACKER.writeNBT(this.handler, null);
		}
		
		@Override
		public void deserializeNBT(INBT nbt) {
			CurrencyCapabilities.SPAWN_TRACKER.readNBT(this.handler, null, nbt);
		}
		
	}
	
}
