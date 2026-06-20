package io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.interfaces;

public interface IWidgetHolder {

    <T> T addChild(T child);
    void removeChild(Object child);

}