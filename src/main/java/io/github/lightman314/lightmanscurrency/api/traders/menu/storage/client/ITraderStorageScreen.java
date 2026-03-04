package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.api.client.gui.tabbed.IEasyTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public interface ITraderStorageScreen extends IEasyTabbedMenuScreen<ITraderStorageMenu, TraderStorageTab,ITraderStorageScreen> {

    ITraderStorageMenu getMenu();
    TraderStorageClientTab<?> getCurrentTab();
    IWidgetPositioner getRightEdgePositioner();
    boolean showRightEdgeWidgets();

    void ChangeTab(ResourceLocation tabKey);
    void ChangeTab(ResourceLocation tabKey,@Nullable LazyPacketData.Builder data);
    void ChangeTab(ResourceLocation tabKey,@Nullable LazyPacketData data);

}
