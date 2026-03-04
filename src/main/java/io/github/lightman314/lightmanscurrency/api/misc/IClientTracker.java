package io.github.lightman314.lightmanscurrency.api.misc;

import net.minecraft.world.entity.Entity;

public interface IClientTracker {

	boolean isClient();
	default boolean isServer() { return !this.isClient(); }

	static IClientTracker entityWrapper(Entity entity) { return () -> entity.level().isClientSide; }
	static IClientTracker forKnown(boolean isClient) { return () -> isClient; }
	static IClientTracker forClient() { return () -> true; }
	static IClientTracker forServer() { return () -> false; }

}