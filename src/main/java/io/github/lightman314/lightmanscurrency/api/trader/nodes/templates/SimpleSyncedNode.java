package io.github.lightman314.lightmanscurrency.api.trader.nodes.templates;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISyncingNode;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;

import java.util.function.Consumer;

public abstract class SimpleSyncedNode extends TraderNode implements ISyncingNode {

    private FancyPacketMap.Mutable changedData = FancyPacketMap.newMutable();
    protected final void setChanged(Consumer<FancyPacketMap.Mutable> writer)
    {
        if(this.isClient() || !this.isSyncReady(this.trader))
            return;
        writer.accept(this.changedData);
        this.setChanged();
    }

    @Override
    public FancyPacketMap getChangedData(ISyncingContext context) { return this.changedData; }

    @Override
    public void clean() { this.changedData = FancyPacketMap.newMutable(); }

}