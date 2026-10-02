package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.TrackingLevel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ISyncingNode extends INodeAccess {

    default boolean isSyncReady() {
        TraderData trader = this.getTrader();
        return trader != null && trader.isInitialized();
    }

    FancyPacketMap getChangedData(ISyncingContext context);
    default boolean sendTo(ISyncingContext context,TrackingLevel oldLevel) {
        TrackingLevel requiredLevel = this.requiredTrackingLevel();
        //Only send if the new level is the required level, but the old level wasn't
        return context.isLevel(requiredLevel) && !oldLevel.isLevel(requiredLevel);
    }
    default TrackingLevel requiredTrackingLevel() { return TrackingLevel.CUSTOMER; }
    void clean();
    void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context);
    default void traderCreatePacket(FancyPacketMap.Mutable builder) {}
    void onDataSync(FancyPacketMap data);

    default void afterTrackingChange(ISyncingContext context,TrackingLevel oldLevel) {}
    default void afterTrackingEnded(ISyncingContext context) {}

    interface SyncingListener extends BiConsumer<Consumer<FancyPacketMap.Mutable>,Predicate<ISyncingContext>> {}

}