package io.github.lightman314.lightmanscurrency.client.gui.util;

import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;

import javax.annotation.Nonnull;

public interface IWidgetPositioner {

    void addWidget(@Nonnull EasyWidget widget);
    default void addWidgets(@Nonnull EasyWidget... widgets)
    {
        for(EasyWidget w : widgets)
            this.addWidget(w);
    }
    void clear();

}