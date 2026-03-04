package io.github.lightman314.lightmanscurrency.api.traders.menu.storage;

import io.github.lightman314.lightmanscurrency.api.traders.data.interfaces.IPersistentTrader;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyMenuTab;
import net.minecraft.resources.ResourceLocation;

public abstract class TraderStorageTab extends EasyMenuTab<ITraderStorageMenu,TraderStorageTab> {

	protected TraderStorageTab(ITraderStorageMenu menu) { super(menu); }

    public final <T extends TraderNode> T getNode(TraderNodeType<T> type) { return this.menu.getTraderNode(type); }

    public final boolean isPersistent() { return this.menu.getTrader() instanceof IPersistentTrader pt && pt.isPersistent(); }

    protected boolean isDefaultTab() { return false; }
    public abstract ResourceLocation tabKey();
    public final int getSortPriority() { return this.isDefaultTab() ? Integer.MIN_VALUE : 0; }
    public final int getTabSlot() { return this.isDefaultTab() ? 0 : this.tabKey().hashCode(); }

}
