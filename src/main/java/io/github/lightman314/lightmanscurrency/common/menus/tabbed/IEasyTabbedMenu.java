package io.github.lightman314.lightmanscurrency.common.menus.tabbed;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

public interface IEasyTabbedMenu<T extends EasyMenuTab<?,T>> extends LazyPacketData.IBuilderProvider, IClientTracker {

    void setMessageListener(Consumer<LazyPacketData> listener);

    T currentTab();
    Map<Integer,T> getAllTabs();

    void ChangeTab(int slot);
    void ChangeTab(int slot, @Nullable LazyPacketData data);
    void ChangeTab(int slot, @Nullable LazyPacketData.Builder data);

}