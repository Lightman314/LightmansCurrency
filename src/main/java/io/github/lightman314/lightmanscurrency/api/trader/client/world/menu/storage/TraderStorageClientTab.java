package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;

import javax.annotation.Nullable;
import java.util.List;

public abstract class TraderStorageClientTab<C extends TraderStorageTab> extends ClientMenuTab<TraderStorageMenu,C,TraderStorageTab,TraderStorageScreen> implements INodeAccess {

    protected TraderStorageClientTab(TraderStorageMenu menu,C commonTab,TraderStorageScreen screen) { super(menu,commonTab,screen); }

    @Nullable
    public final TraderData getTrader() { return this.getMenu().getTrader(); }
    @Nullable
    @Override
    public final <T extends TraderNode> T getNode(TraderNodeType<T> type) { return this.getCommonTab().getNode(type); }
    @Override
    public final List<TraderNode> getAllNodes() { return this.getCommonTab().getAllNodes(); }
    public final <T> T getPermission(Permission<T> permission) { return this.getCommonTab().getPermission(permission); }

    @Override
    public boolean isVisible() { return this.getCommonTab().canOpen(); }

}