package io.github.lightman314.lightmanscurrency.api.traders.menu.storage;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.IEasyTabbedMenuScreen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITraderStorageScreen extends IEasyTabbedMenuScreen<ITraderStorageMenu,TraderStorageTab,ITraderStorageScreen> {

    @Nonnull
    ITraderStorageMenu getMenu();

    @Deprecated
    void changeTab(int tab);
    @Deprecated
    void changeTab(int newTab, boolean sendMessage, @Nullable LazyPacketData.Builder selfMessage);

}
