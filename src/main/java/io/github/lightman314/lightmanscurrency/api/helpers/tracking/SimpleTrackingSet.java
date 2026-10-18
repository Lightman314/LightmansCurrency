package io.github.lightman314.lightmanscurrency.api.helpers.tracking;

import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.*;

public class SimpleTrackingSet {

    private final Map<UUID,Entry> data = new HashMap<>();
    private final Map<Long,UUID> keyToPlayerMap = new HashMap<>();

    private final MutableLong keyGetter;
    public SimpleTrackingSet() { this.keyGetter = new MutableLong(0L); }
    public SimpleTrackingSet(MutableLong keyGetter) { this.keyGetter = keyGetter; }

    public boolean isEmpty() { return this.data.isEmpty(); }
    public Set<UUID> getTrackingPlayers() { return Set.copyOf(this.data.keySet()); }

    public boolean changedSinceLastTracking(UUID playerID) { return this.data.getOrDefault(playerID,new Entry()).changed; }
    public boolean isTracking(UUID playerID) { return this.data.getOrDefault(playerID,new Entry()).trackingKeys.isEmpty(); }

    public long requestTracking(UUID playerID) {
        Entry entry = this.data.computeIfAbsent(playerID,p -> new Entry());
        long newKey = this.keyGetter.getAndIncrement();
        entry.add(newKey);
        this.keyToPlayerMap.put(newKey,playerID);
        return newKey;
    }

    public void endTracking(long trackingKey) {
        UUID playerID = this.keyToPlayerMap.get(trackingKey);
        if(playerID == null)
            return;
        Entry entry = this.data.computeIfAbsent(playerID,p -> new Entry());
        entry.remove(trackingKey);
        this.keyToPlayerMap.remove(trackingKey);
    }

    public void setChanged() { this.data.values().forEach(Entry::setChanged); }

    public void deletePlayer(Player player) { this.deletePlayer(player.getUUID()); }
    public void deletePlayer(UUID playerID) {
        Entry entry = this.data.get(playerID);
        if(entry != null) {
            for(long key : entry.trackingKeys)
                this.keyToPlayerMap.remove(key);
            this.data.remove(playerID);
        }
    }

    private static class Entry {
        private boolean changed = true;
        private final Set<Long> trackingKeys = new HashSet<>();
        private boolean isEmpty() { return this.trackingKeys.isEmpty(); }
        private boolean isTracking() { return !this.trackingKeys.isEmpty(); }
        private void setChanged() { if(this.trackingKeys.isEmpty()) this.changed = true; }
        private Entry() {}

        private void add(long newKey) { this.trackingKeys.add(newKey); this.changed = false; }
        private void remove(long key) { this.trackingKeys.remove(key); }

    }

}