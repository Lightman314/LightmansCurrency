package io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.FancyWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;

public interface IPositionalWidgetHolder {

    <T extends FancyWidget> T addChild(ScreenPosition position, T child);

}