package io.github.lightman314.lightmanscurrency.common.menus.tabbed;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.RegistryAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

public interface IEasyTabbedMenu<T extends EasyMenuTab<?,T>> extends LazyPacketData.IBuilderProvider, IClientTracker {

    @Nonnull
    RegistryAccess registryAccess();

    void setMessageListener(@Nonnull Consumer<LazyPacketData> listener);

    @Nonnull
    Map<Integer,T> getAllTabs();

    void ChangeTab(int slot);
    void ChangeTab(int slot, @Nullable LazyPacketData data);
    void ChangeTab(int slot, @Nullable LazyPacketData.Builder data);

}
