package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

public interface IClientUpdateListener<T> {
	public void onClientUpdated();
	public boolean isApplicable(T object);
}
