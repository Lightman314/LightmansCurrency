package io.github.lightman314.lightmanscurrency.common.util;

public interface IClientTracker {

	public boolean isClient();
	public default boolean isServer() { return !this.isClient(); }
	
}