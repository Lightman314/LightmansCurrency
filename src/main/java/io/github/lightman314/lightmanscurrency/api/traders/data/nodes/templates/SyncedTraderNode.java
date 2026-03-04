package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.ISyncingNode;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public abstract class SyncedTraderNode extends TraderNode implements ISyncingNode {

    private LazyPacketData.Builder changedData = null;
    protected final void setChanged(Consumer<LazyPacketData.Builder> dataWriter)
    {
        if(this.trader == null)
            return;
        if(this.changedData == null)
            this.changedData = this.builder();
        dataWriter.accept(this.changedData);
        this.trader.setChanged(this);
    }
    @Override
    public final LazyPacketData.Builder getChangedData(Player player) { return this.changedData; }
    @Override
    public final void clean() { this.changedData = null; }
}
