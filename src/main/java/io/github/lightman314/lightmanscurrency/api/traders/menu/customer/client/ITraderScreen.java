package io.github.lightman314.lightmanscurrency.api.traders.menu.customer.client;

import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderMenu;
import io.github.lightman314.lightmanscurrency.api.client.gui.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.api.client.widgets.IWidgetPositioner;

public interface ITraderScreen extends IEasyScreen {

    ITraderMenu getMenu();
    void setTab(TraderClientTab tab);
    void closeTab();

    IWidgetPositioner getRightEdgePositioner();

}
