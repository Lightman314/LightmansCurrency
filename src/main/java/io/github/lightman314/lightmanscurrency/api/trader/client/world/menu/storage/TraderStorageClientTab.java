package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage;

import io.github.lightman314.lightmanscurrency.api.client.gui.screen.menu.tabbed.ClientMenuTab;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.positioner.IMoveableWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage.TraderStorageTab;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.List;

public abstract class TraderStorageClientTab<C extends TraderStorageTab> extends ClientMenuTab<TraderStorageMenu,C,TraderStorageTab,TraderStorageScreen> implements INodeAccess, IPermissionAccess {

    private final List<IMoveableWidget> rightEdgeWidgets = new ArrayList<>();

    protected TraderStorageClientTab(TraderStorageMenu menu,C commonTab,TraderStorageScreen screen) { super(menu,commonTab,screen); }

    @Nullable
    public final TraderData getTrader() { return this.getMenu().getTrader(); }
    @Nullable
    @Override
    public final <T extends TraderNode> T getNode(TraderNodeType<T> type) { return this.getCommonTab().getNode(type); }
    @Override
    public final List<TraderNode> getAllNodes() { return this.getCommonTab().getAllNodes(); }
    @Override
    public final <T> T getPermission(Permission<T> permission) { return this.getCommonTab().getPermission(permission); }

    @Override
    public boolean isVisible() { return this.getCommonTab().canOpen(); }

    public boolean showRightEdgeWidgets() { return true; }

    public <T extends IMoveableWidget> T addRightEdgeWidget(T widget) {
        this.addChild(widget);
        this.rightEdgeWidgets.add(widget);
        this.getScreen().rightSidePositioner().addWidgets(widget);
        return widget;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onTabOpened(FancyPacketMap message) {
        this.rightEdgeWidgets.clear();
        super.onTabOpened(message);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onTabClosed() {
        super.onTabClosed();
        for(IMoveableWidget widget : new ArrayList<>(this.rightEdgeWidgets))
            this.getScreen().rightSidePositioner().removeWidget(widget);
        this.rightEdgeWidgets.clear();
    }

    public abstract static class Invisible<C extends TraderStorageTab> extends TraderStorageClientTab<C> {

        protected Invisible(TraderStorageMenu menu, C commonTab, TraderStorageScreen screen) {
            super(menu, commonTab, screen);
        }
        @Override
        public final boolean isVisible() { return false; }
        @Override
        public final IconData getIcon() { return IconData.empty(); }
        @Override
        public Component getName() { return Component.empty(); }

    }

}