package io.github.lightman314.lightmanscurrency.api.trader.tracking;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public interface ISyncingContext {

    /**
     * For debug use only, sends full syncing data
     */
    ISyncingContext FULL = new Full();

    UUID getPlayerID();
    TrackingLevel getPlayerTrackingLevel();
    default boolean isLevel(TrackingLevel level) { return this.getPlayerTrackingLevel().isLevel(level); }
    TrackingLevel getSpecialRequestLevel(UUID playerID);
    Set<UUID> getSpecialCustomerSet();
    default Set<UUID> getCustomerSet()
    {
        if(this.isLevel(TrackingLevel.CUSTOMER) && this.addedSpecialRequest() != null)
        {
            Set<UUID> combinedSet = new HashSet<>();
            combinedSet.add(this.getPlayerID());
            combinedSet.addAll(this.getSpecialCustomerSet());
            return combinedSet;
        }
        else
            return this.getSpecialCustomerSet();
    }
    default boolean isSpecialRequest() { return this.addedSpecialRequest() != null; }
    @Nullable
    Pair<UUID,TrackingLevel> addedSpecialRequest();
    default boolean isValidTarget(UUID id) { return this.isValidTarget(id,TrackingLevel.CUSTOMER); }
    default boolean isValidTarget(UUID id,TrackingLevel minLevel)
    {
        if(minLevel == TrackingLevel.NONE)
            return true;
        var specialRequest = this.addedSpecialRequest();
        if(specialRequest != null)
            return specialRequest.getFirst().equals(id) && specialRequest.getSecond().isLevel(minLevel);
        return (this.getPlayerID().equals(id) && this.getPlayerTrackingLevel().isLevel(minLevel)) || this.getSpecialRequestLevel(id).isLevel(minLevel);
    }

    record Simple(Player player) implements ISyncingContext
    {
        @Override
        public UUID getPlayerID() { return this.player.getUUID(); }
        @Override
        public TrackingLevel getPlayerTrackingLevel() { return TrackingLevel.NONE; }
        @Override
        public TrackingLevel getSpecialRequestLevel(UUID playerID) { return TrackingLevel.NONE; }
        @Override
        public Set<UUID> getSpecialCustomerSet() { return Set.of(); }
        @Nullable
        @Override
        public Pair<UUID, TrackingLevel> addedSpecialRequest() { return null; }
    }

    class Full implements ISyncingContext
    {
        @Override
        public UUID getPlayerID() { return new UUID(0,0); }
        @Override
        public TrackingLevel getPlayerTrackingLevel() { return TrackingLevel.STORAGE; }
        @Override
        public TrackingLevel getSpecialRequestLevel(UUID playerID) { return TrackingLevel.STORAGE; }
        @Override
        public Set<UUID> getSpecialCustomerSet() { return Set.of(); }
        @Nullable
        @Override
        public Pair<UUID,TrackingLevel> addedSpecialRequest() { return null; }
        @Override
        public boolean isValidTarget(UUID id, TrackingLevel minLevel) { return true; }
    }

}