package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.tracking.TrackingLevel;
import org.jetbrains.annotations.ApiStatus;

public interface ISyncingNode {

    default boolean isSyncReady(TraderData trader) { return trader != null && trader.isInitialized(); }

    LazyPacketData.Builder getChangedData(ISyncingContext context);
    default boolean sendTo(ISyncingContext context,TrackingLevel oldLevel) {
        return this.isStorageOnly() ? context.isLevel(TrackingLevel.STORAGE) : context.isLevel(TrackingLevel.CUSTOMER);
    }
    default boolean isStorageOnly() { return false; }
    void clean();
    /**
     * Should be overriden, but never called directly
     */
    @ApiStatus.Internal
    void onDataSync(LazyPacketData data);
    void createSyncPacket(LazyPacketData.Builder builder,ISyncingContext context);
    default void afterTrackingChange(ISyncingContext context,TrackingLevel oldLevel) {}
    default void afterTrackingEnded(ISyncingContext playerID) {}

}
