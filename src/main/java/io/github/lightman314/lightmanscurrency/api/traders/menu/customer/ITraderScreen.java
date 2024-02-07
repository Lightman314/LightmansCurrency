package io.github.lightman314.lightmanscurrency.api.traders.menu.customer;

import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;

import javax.annotation.Nonnull;

public interface ITraderScreen extends IEasyScreen {

    @Nonnull
    ITraderMenu getMenu();
    void setTab(@Nonnull TraderClientTab tab);
    void closeTab();

}
