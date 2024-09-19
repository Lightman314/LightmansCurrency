package io.github.lightman314.lightmanscurrency.common.util;

import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;

public interface IClientTracker {

	boolean isClient();
	default boolean isServer() { return !this.isClient(); }

	@Nonnull
	static IClientTracker entityWrapper(@Nonnull Entity entity) { return () -> entity.level().isClientSide; }
	@Nonnull
	static IClientTracker forKnown(boolean isClient) { return () -> isClient; }
	@Nonnull
	static IClientTracker forClient() { return () -> true; }
	@Nonnull
	static IClientTracker forServer() { return () -> false; }

}