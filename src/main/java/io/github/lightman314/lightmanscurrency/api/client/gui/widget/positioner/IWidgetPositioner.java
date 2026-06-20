package io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IRenderTick;

public interface IWidgetPositioner extends IRenderTick {

    void addWidget(IMoveableWidget widget);
    void clearWidgets();
    //Tick late by default so that its children have a chance to update their visibility
    @Override
    default boolean renderTickLate() { return true; }
}