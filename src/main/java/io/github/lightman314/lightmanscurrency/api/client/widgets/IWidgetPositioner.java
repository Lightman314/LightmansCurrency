package io.github.lightman314.lightmanscurrency.api.client.widgets;

import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyWidget;

public interface IWidgetPositioner {

    void addWidget(EasyWidget widget);
    default void addWidgets(EasyWidget... widgets)
    {
        for(EasyWidget w : widgets)
            this.addWidget(w);
    }
    void removeWidget(EasyWidget widget);
    default void removeWidgets(EasyWidget... widgets)
    {
        for(EasyWidget w : widgets)
            this.removeWidget(w);
    }
    void clear();

}
