package io.github.lightman314.lightmanscurrency.api.traders.menu.storage;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITraderStorageScreen extends IEasyScreen {

    @Nonnull
    ITraderStorageMenu getMenu();

    void changeTab(int tab);
    void changeTab(int newTab, boolean sendMessage, @Nullable LazyPacketData.Builder selfMessage);
    void selfMessage(@Nonnull LazyPacketData.Builder message);

}
