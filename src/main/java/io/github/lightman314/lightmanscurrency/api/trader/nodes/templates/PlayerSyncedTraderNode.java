package io.github.lightman314.lightmanscurrency.api.trader.nodes.templates;

import com.google.common.base.Predicates;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISyncingNode;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class PlayerSyncedTraderNode extends TraderNode implements ISyncingNode {

    private final List<BiConsumer<ISyncingContext,FancyPacketMap.Mutable>> changedData = new ArrayList<>();
    protected final void setChanged(Consumer<FancyPacketMap.Mutable> writer) { this.setChanged(writer, Predicates.alwaysTrue()); }
    protected final void setChanged(Consumer<FancyPacketMap.Mutable> writer,UUID playerID) { this.setChanged(writer,context -> context.isValidTarget(playerID)); }
    protected final void setChanged(Consumer<FancyPacketMap.Mutable> writer,PlayerReference player) { this.setChanged(writer, context -> context.isValidTarget(player.id)); }
    protected final void setChanged(Consumer<FancyPacketMap.Mutable> writer,Predicate<ISyncingContext> filter)
    {
        this.setChanged((context,builder) -> {
            if(filter.test(context))
                writer.accept(builder);
        });
    }
    protected final void setChanged(BiConsumer<ISyncingContext,FancyPacketMap.Mutable> writer)
    {
        if(this.isClient() || !this.isSyncReady(this.trader))
            return;
        this.changedData.add(writer);
        this.setChanged();
    }

    @Override
    public FancyPacketMap getChangedData(ISyncingContext context) {
        FancyPacketMap.Mutable builder = FancyPacketMap.newMutable();
        for(var writer : new ArrayList<>(this.changedData))
            writer.accept(context,builder);
        return builder;
    }

    @Override
    public void clean() { this.changedData.clear(); }

}