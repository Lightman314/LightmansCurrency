package io.github.lightman314.lightmanscurrency.api.traders.tracking;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;

public class TraderTrackingData {

    private long nextID = 0;
    private final Map<UUID,PlayerData> data = new HashMap<>();

    public TrackingLevel getLevel(Player player) {
        PlayerData data = this.data.get(player.getUUID());
        return data == null ? TrackingLevel.NONE : data.getHighestLevel();
    }

    public Result requestTracking(Player player,TrackingLevel level)
    {
        if(level == TrackingLevel.NONE)
            return new Result(-1,null);
        PlayerData data = this.data.get(player.getUUID());
        boolean flag = false;
        if(data == null)
        {
            data = new PlayerData();
            this.data.put(player.getUUID(),data);
            flag = true;
        }
        long key = data.requestTracking(level);
        return new Result(data.requestTracking(level),flag ? null : data.cleanChangedNodes());
    }

    public void endTracking(Player player,long key)
    {
        PlayerData data = this.data.get(player.getUUID());
        if(data != null)
            data.endTracking(key);
    }

    public void clearPlayer(UUID playerID) { this.data.remove(playerID); }

    public void afterNodeChanged(TraderNodeType<?> type)
    {
        this.data.forEach((player,data) -> {
            if(data.getHighestLevel() == TrackingLevel.NONE)
                data.changedSinceLastPacket.add(type);
        });
    }
    @Nullable
    public Set<TraderNodeType<?>> getChangedNodes(Player player)
    {
        PlayerData data = this.data.get(player.getUUID());
        if(data == null)
            return new HashSet<>();
        return data.changedSinceLastPacket;
    }

    private class PlayerData
    {
        final Map<TrackingLevel,Set<Long>> trackingMap = new HashMap<>();
        final Set<TraderNodeType<?>> changedSinceLastPacket = new HashSet<>();
        TrackingLevel getHighestLevel()
        {
            if(!this.getOrCreateSet(TrackingLevel.STORAGE).isEmpty())
                return TrackingLevel.STORAGE;
            if(!this.getOrCreateSet(TrackingLevel.CUSTOMER).isEmpty())
                return TrackingLevel.CUSTOMER;
            return TrackingLevel.NONE;
        }
        Set<Long> getOrCreateSet(TrackingLevel level)
        {
            if(!this.trackingMap.containsKey(level))
                this.trackingMap.put(level,new HashSet<>());
            return this.trackingMap.get(level);
        }
        long requestTracking(TrackingLevel level)
        {
            Set<Long> set = this.getOrCreateSet(level);
            set.add(TraderTrackingData.this.nextID++);
            return nextID;
        }
        void endTracking(long key)
        {
            this.getOrCreateSet(TrackingLevel.STORAGE).remove(key);
            this.getOrCreateSet(TrackingLevel.CUSTOMER).remove(key);
        }
        Set<TraderNodeType<?>> cleanChangedNodes() {
            Set<TraderNodeType<?>> result = new HashSet<>(this.changedSinceLastPacket);
            this.changedSinceLastPacket.clear();
            return result;
        }
    }

    public record Result(long key,@Nullable Set<TraderNodeType<?>> changedNodes) { }

}
