package io.github.lightman314.lightmanscurrency.common.menus.tabbed;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IEasyTabbedMenu<T extends EasyMenuTab<?,T>> extends LazyPacketData.IBuilderProvider, IClientTracker {

    
    RegistryAccess registryAccess();

    void setMessageListener(Consumer<LazyPacketData> listener);

    T currentTab();
    Map<Integer,T> getAllTabs();

    void ChangeTab(int slot);
    void ChangeTab(int slot, @Nullable LazyPacketData data);
    void ChangeTab(int slot, @Nullable LazyPacketData.Builder data);

}
