package io.github.lightman314.lightmanscurrency.api.trader.client.nodes;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IWidgetPositioner;
import io.github.lightman314.lightmanscurrency.api.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;

public interface ICustomerScreenListener {

    void onCustomerScreenInit(TraderSource source,IWidgetPositioner edgePositioner,IWidgetHolder screen);
    void onCustomerScreenRender(TraderSource source,FancyGuiExtractor gui, ScreenArea area);

}