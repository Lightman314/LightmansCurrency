package io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyMenuTab;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.IEasyTabbedMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IEasyTabbedMenuScreen<X extends IEasyTabbedMenu<T>,T extends EasyMenuTab<X,T>,S extends IEasyTabbedMenuScreen<X,T,S>> extends IEasyScreen {

    int getCurrentTabIndex();

    @Nonnull
    X getMenuInterface();

    void ChangeTab(int tab);
    void ChangeTab(int tab, @Nullable LazyPacketData.Builder data);
    void ChangeTab(int tab, @Nullable LazyPacketData data, boolean sendPacket);

}