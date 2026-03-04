package io.github.lightman314.lightmanscurrency.api.traders.menu.storage;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public abstract class TraderStorageNodeTab<T extends TraderNode> extends TraderStorageTab {

    private final TraderNodeType<T> type;
    protected TraderStorageNodeTab(TraderNodeType<T> type,ITraderStorageMenu menu) {
        super(menu);
        this.type = type;
    }

    @Nullable
    public final T getNode() { return this.menu.getTraderNode(this.type); }

    @Override
    public final boolean canOpen(Player player) { return this.getNode() != null && this.canOpenTab(player); }

    protected boolean canOpenTab(Player player) { return true; }

}
