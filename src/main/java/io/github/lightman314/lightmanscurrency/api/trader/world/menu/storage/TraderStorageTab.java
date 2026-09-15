package io.github.lightman314.lightmanscurrency.api.trader.world.menu.storage;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.ISortedTab;
import io.github.lightman314.lightmanscurrency.api.world.menu.tabbed.MenuTab;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.List;

public abstract class TraderStorageTab extends MenuTab<TraderStorageMenu> implements ISortedTab, INodeAccess, IPermissionAccess {

    @Nullable
    public final TraderData getTrader() { return this.getMenu().getTrader(); }
    @Nullable
    @Override
    public final <T extends TraderNode> T getNode(TraderNodeType<T> type) {
        TraderData trader = this.getTrader();
        return trader == null ? null : trader.getNode(type);
    }
    @Override
    public List<TraderNode> getAllNodes() {
        TraderData trader = this.getTrader();
        return trader == null ? ImmutableList.of() : trader.getAllNodes();
    }

    @Override
    public final <T> T getPermission(Permission<T> permission) {
        TraderData trader = this.getTrader();
        if(trader == null)
            return permission.getEmpty();
        return trader.getPermission(this.getPlayer(),permission);
    }

    public TraderStorageTab(TraderStorageMenu menu) { super(menu); }

    public abstract Identifier getKey();
    protected boolean isDefaultTab() { return false; }
    public int getTabSlot() { return this.getKey().hashCode(); }

    @Override
    public Identifier getClientTabKey() { return this.getKey(); }

    @Override
    public abstract boolean canOpen();

}
