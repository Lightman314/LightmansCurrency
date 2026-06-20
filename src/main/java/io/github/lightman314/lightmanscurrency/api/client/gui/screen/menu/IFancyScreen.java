package io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenPosition;

public interface IFancyScreen extends IWidgetHolder {

    ScreenPosition getCorner();
    ScreenArea getArea();
    int getWidth();
    int getHeight();

}