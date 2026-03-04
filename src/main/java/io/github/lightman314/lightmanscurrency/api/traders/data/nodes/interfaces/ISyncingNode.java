package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.tracking.TrackingLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

public interface ISyncingNode {

    LazyPacketData.Builder getChangedData(Player player);
    default boolean sendTo(Player player,TrackingLevel level) {
        return this.isStorageOnly() ? level.isLevel(TrackingLevel.STORAGE) : level.isLevel(TrackingLevel.CUSTOMER);
    }
    default boolean isStorageOnly() { return false; }
    void clean();
    /**
     * Should be overriden, but never called directly
     */
    @ApiStatus.Internal
    void onDataSync(LazyPacketData data);
    void createSyncPacket(LazyPacketData.Builder builder,Player player);
    default void afterTrackingChange(Player player,TrackingLevel oldLevel,TrackingLevel newLevel) {}
    default void afterTrackingEnded(UUID playerID) {}

}
