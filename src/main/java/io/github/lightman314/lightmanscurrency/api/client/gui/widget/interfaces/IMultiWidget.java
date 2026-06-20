package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.interfaces.IWidgetHolder;

public interface IMultiWidget extends IWidgetHolder {

    void defineParent(IWidgetHolder screen);
    void addEarlyChildren();
    void addLateChildren();
    void removeChildren();

}