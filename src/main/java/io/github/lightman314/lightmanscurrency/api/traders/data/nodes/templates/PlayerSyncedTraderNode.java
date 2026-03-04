package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.ISyncingNode;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.*;

public abstract class PlayerSyncedTraderNode extends TraderNode implements ISyncingNode {

    private final List<BiConsumer<Player,LazyPacketData.Builder>> changedData = new ArrayList<>();
    protected final void setChanged(Consumer<LazyPacketData.Builder> dataWriter) { this.setChanged(dataWriter,p -> true); }
    protected final void setChanged(Consumer<LazyPacketData.Builder> dataWriter,UUID playerID) { this.setChanged(dataWriter,player -> player.getUUID().equals(playerID)); }
    protected final void setChanged(Consumer<LazyPacketData.Builder> dataWriter,PlayerReference player) { this.setChanged(dataWriter,player::is); }
    protected final void setChanged(Consumer<LazyPacketData.Builder> dataWriter,Predicate<Player> filter)
    {
        if(this.trader == null)
            return;
        this.changedData.add((player,builder) -> {
            if(filter.test(player))
                dataWriter.accept(builder);
        });
        this.trader.setChanged(this);
    }
    protected final void setChanged(BiConsumer<Player,LazyPacketData.Builder> dataWriter)
    {
        this.changedData.add(dataWriter);
        this.trader.setChanged(this);
    }

    @Override
    public LazyPacketData.Builder getChangedData(Player player) {
        LazyPacketData.Builder builder = this.builder();
        for(var writer : new ArrayList<>(this.changedData))
            writer.accept(player,builder);
        return builder;
    }

    @Override
    public void clean() { this.changedData.clear(); }

}