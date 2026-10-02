package io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;

public interface ScrollingWidgetBuilder {

    int buildWidgets(IPositionalWidgetHolder holder,int width,int yPos);

}