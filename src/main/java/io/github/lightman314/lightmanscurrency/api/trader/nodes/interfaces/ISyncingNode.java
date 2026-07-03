package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.TrackingLevel;

public interface ISyncingNode {

    default boolean isSyncReady(TraderData trader) { return trader != null && trader.isInitialized(); }

    FancyPacketMap getChangedData(ISyncingContext context);
    default boolean sendTo(ISyncingContext context,TrackingLevel oldLevel) {
        if(this.isStorageOnly())
            return context.isLevel(TrackingLevel.STORAGE);
        else //Don't send to the player if both the new and old levels are customer
            return context.isLevel(TrackingLevel.CUSTOMER) && !oldLevel.isLevel(TrackingLevel.CUSTOMER);
    }
    default boolean isStorageOnly() { return false; }
    void clean();
    void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context);
    default void traderCreatePacket(FancyPacketMap.Mutable builder) {}
    void onDataSync(FancyPacketMap data);

    default void afterTrackingChange(ISyncingContext context,TrackingLevel oldLevel) {}
    default void afterTrackingEnded(ISyncingContext context) {}

}