package io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner;

import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;

public interface IMoveableWidget {

    void move(ScreenPosition position,WidgetFacing facing);
    boolean isVisible();
    void hideThisFrame();

}
