package io.github.lightman314.lightmanscurrency.client.gui.util;

import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
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
