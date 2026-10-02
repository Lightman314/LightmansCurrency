package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

public interface IRemovalListener {

    void afterWidgetRemoval();

    static IRemovalListener simple(Runnable listener) { return listener::run; }

}
