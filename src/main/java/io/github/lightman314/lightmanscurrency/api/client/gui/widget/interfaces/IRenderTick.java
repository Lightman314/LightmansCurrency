package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;

public interface IRenderTick {

    default boolean renderTickLate() { return false; }
    void renderTick(ScreenPosition mousePos);

}