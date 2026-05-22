package io.github.lightman314.lightmanscurrency.api.traders.menu.storage;

import io.github.lightman314.lightmanscurrency.api.traders.data.interfaces.IPersistentTrader;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.common.menus.tabbed.EasyMenuTab;
import net.minecraft.resources.ResourceLocation;

public abstract class TraderStorageTab extends EasyMenuTab<ITraderStorageMenu,TraderStorageTab> {

    public static final int SORT_STORAGE = -1000;
    public static final int SORT_UPGRADES = -900;
    public static final int SORT_MONEY_STORAGE = -750;
    public static final int SORT_INFO = -500;
    public static final int SORT_SETTINGS = -250;
    public static final int SORT_SETINGS_CLIPBOARD = 750;
    public static final int SORT_TRADER_RULES = 1000;

	protected TraderStorageTab(ITraderStorageMenu menu) { super(menu); }

    public final <T extends TraderNode> T getNode(TraderNodeType<T> type) { return this.menu.getTraderNode(type); }

    public final boolean isPersistent() { return this.menu.getTrader() instanceof IPersistentTrader pt && pt.isPersistent(); }

    protected boolean isDefaultTab() { return false; }
    public abstract ResourceLocation tabKey();
    public int getSortPriority() { return this.isDefaultTab() ? Integer.MIN_VALUE : 0; }
    public final int getTabSlot() { return this.isDefaultTab() ? 0 : this.tabKey().hashCode(); }

}
