package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

public interface IKeyboardInterceptor {

    default boolean preventInventoryButtonFromClosingScreen() { return true; }

}
