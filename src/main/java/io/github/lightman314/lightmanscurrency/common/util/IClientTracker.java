package io.github.lightman314.lightmanscurrency.common.util;

public interface IClientTracker {

	boolean isClient();
	default boolean isServer() { return !this.isClient(); }
	
}
