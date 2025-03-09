package io.github.lightman314.lightmanscurrency.api.traders.menu.storage;

import io.github.lightman314.lightmanscurrency.client.gui.easy.tabbed.IEasyTabbedMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.util.IWidgetPositioner;
import net.minecraft.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
public interface ITraderStorageScreen extends IEasyTabbedMenuScreen<ITraderStorageMenu,TraderStorageTab,ITraderStorageScreen> {

    ITraderStorageMenu getMenu();
    IWidgetPositioner getRightEdgePositioner();
    boolean showRightEdgeWidgets();

}
