package io.github.lightman314.lightmanscurrency.api.traders.menu.storage;

import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.IEasyTabbedMenuScreen;

import javax.annotation.Nonnull;

public interface ITraderStorageScreen extends IEasyTabbedMenuScreen<ITraderStorageMenu,TraderStorageTab,ITraderStorageScreen> {

    @Nonnull
    ITraderStorageMenu getMenu();

}
