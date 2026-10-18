package io.github.lightman314.lightmanscurrency.api.helpers.tracking;

import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UUIDTrackingSet {

    private final MutableLong nextKey = new MutableLong(0L);
    private final Map<UUID,SimpleTrackingSet> sets = new HashMap<>();
    private final Map<Long,UUID> keyToSetMap = new HashMap<>();

    private SimpleTrackingSet getSetOrDummy(UUID entryID) {
        return this.sets.getOrDefault(entryID,new SimpleTrackingSet());
    }

    private SimpleTrackingSet getOrCreateSet(UUID entryID) {
        return this.sets.computeIfAbsent(entryID,i -> new SimpleTrackingSet(this.nextKey));
    }

    public void setChanged(UUID entryID) { this.getSetOrDummy(entryID).setChanged(); }

    public boolean shouldSendPacket(UUID entryID,UUID playerID) {
        return this.getSetOrDummy(playerID).changedSinceLastTracking(playerID);
    }

    public Set<UUID> getTrackingPlayers(UUID entryID) { return this.getSetOrDummy(entryID).getTrackingPlayers(); }
    public boolean isTracking(UUID entryID,Player player) { return this.isTracking(entryID,player.getUUID()); }
    public boolean isTracking(UUID entryID,UUID playerID) { return this.getSetOrDummy(entryID).isTracking(playerID); }

    public TrackingResult requestTracking(UUID entryID,Player player) { return this.requestTracking(entryID,player.getUUID()); }
    public TrackingResult requestTracking(UUID entryID,UUID playerID) {
        SimpleTrackingSet tracking = this.getOrCreateSet(entryID);
        boolean sendPacket = tracking.changedSinceLastTracking(playerID);
        long newKey = tracking.requestTracking(playerID);
        this.keyToSetMap.put(newKey,entryID);
        return new TrackingResult(sendPacket,newKey);
    }

    public void endTracking(long trackingKey) {
        UUID entryID = this.keyToSetMap.get(trackingKey);
        if(entryID != null) {
            SimpleTrackingSet tracking = this.getSetOrDummy(entryID);
            tracking.endTracking(trackingKey);
            this.keyToSetMap.remove(trackingKey);
        }
    }

    public void forgetPlayer(Player player) { this.forgetPlayer(player.getUUID()); }
    public void forgetPlayer(UUID playerID) {
        this.sets.forEach((id,tracking) -> tracking.deletePlayer(playerID));
    }

    public void clearCacheIfEmpty(UUID entryID) {
        if(this.sets.containsKey(entryID) && this.sets.get(entryID).isEmpty())
            this.sets.remove(entryID);
    }

    public record TrackingResult(boolean sendFreshPacket,long trackingKey) { }

}