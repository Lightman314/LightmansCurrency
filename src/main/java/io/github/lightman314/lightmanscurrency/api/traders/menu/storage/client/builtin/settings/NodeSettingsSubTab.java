package io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;

import javax.annotation.Nullable;

public abstract class NodeSettingsSubTab<T extends TraderNode> extends SettingsSubTab {

    private final TraderNodeType<T> type;
    protected NodeSettingsSubTab(TraderNodeType<T> type, TraderSettingsClientTab parent) { super(parent); this.type = type; }

    @Nullable
    protected final T getNode() { return this.menu.getTraderNode(this.type); }

    @Override
    public final boolean canOpen() {
        T node = this.getNode();
        return node != null && this.canOpen(node);
    }

    protected abstract boolean canOpen(T node);

}
