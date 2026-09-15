package io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IRenderTick;

import java.util.List;

public interface IWidgetPositioner extends IRenderTick {

    default void addWidgets(IMoveableWidget... widgets) { this.addWidgets(List.of(widgets)); }
    void addWidgets(List<? extends IMoveableWidget> widgets);
    void removeWidget(IMoveableWidget widget);
    void clearWidgets();
    //Tick late by default so that its children have a chance to update their visibility
    @Override
    default boolean renderTickLate() { return true; }
}