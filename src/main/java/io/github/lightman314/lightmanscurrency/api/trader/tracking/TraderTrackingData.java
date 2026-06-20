package io.github.lightman314.lightmanscurrency.api.trader.tracking;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
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
    public TrackingLevel getSpecialLevel(Player player,UUID target)
    {
        PlayerData data = this.data.get(player.getUUID());
        return data == null ? TrackingLevel.NONE : data.getHighestSpecialLevel(target);
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
        return new Result(key,flag ? null : data.cleanChangedNodes());
    }
    public long requestSpecialTracking(Player player,TrackingLevel level,UUID target)
    {
        if(level == TrackingLevel.NONE)
            return -1;
        PlayerData data = this.data.computeIfAbsent(player.getUUID(), k -> new PlayerData());
        return data.requestSpecialTracking(target,level);
    }

    public ISyncingContext getContext(Player player)
    {
        return new Context(player,this.data.getOrDefault(player.getUUID(),new PlayerData()),null);
    }
    public ISyncingContext getSpecialContext(Player player,UUID newTarget)
    {
        return new Context(player,this.data.getOrDefault(player.getUUID(),new PlayerData()),newTarget);
    }

    public boolean endTracking(Player player,long key)
    {
        PlayerData data = this.data.get(player.getUUID());
        if(data != null)
            return data.endTracking(key);
        return false;
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
            return null;
        return data.changedSinceLastPacket;
    }

    private class PlayerData
    {
        final Map<TrackingLevel,Set<Long>> trackingMap = new HashMap<>();
        final Map<Long,UUID> keyToSpecialMap = new HashMap<>();
        final Map<UUID,Map<TrackingLevel,Set<Long>>> specialTrackingMap = new HashMap<>();
        final Set<TraderNodeType<?>> changedSinceLastPacket = new HashSet<>();
        Set<Long> getOrCreateSet(TrackingLevel level) { return this.trackingMap.computeIfAbsent(level,l -> new HashSet<>()); }
        TrackingLevel getHighestLevel()
        {
            for(TrackingLevel level : TrackingLevel.HIGHEST_TO_LOWEST)
            {
                if(!this.getOrCreateSet(level).isEmpty())
                    return level;
            }
            return TrackingLevel.NONE;
        }
        Set<Long> getOrCreateSpecialSet(UUID target,TrackingLevel level) { return this.specialTrackingMap.computeIfAbsent(target,t -> new HashMap<>()).computeIfAbsent(level,l -> new HashSet<>()); }
        TrackingLevel getHighestSpecialLevel(UUID target)
        {
            for(TrackingLevel level : TrackingLevel.HIGHEST_TO_LOWEST)
            {
                if(!this.getOrCreateSpecialSet(target,level).isEmpty())
                    return level;
            }
            return TrackingLevel.NONE;
        }
        private long getNextID() { return TraderTrackingData.this.nextID++; }
        long requestTracking(TrackingLevel level)
        {
            Set<Long> set = this.getOrCreateSet(level);
            long id = this.getNextID();
            set.add(id);
            return id;
        }
        boolean endTracking(long key)
        {
            if(this.keyToSpecialMap.containsKey(key))
            {
                this.endSpecialTracking(this.keyToSpecialMap.get(key),key);
                return true;
            }
            else
            {
                for(TrackingLevel level : TrackingLevel.HIGHEST_TO_LOWEST)
                    this.getOrCreateSet(level).remove(key);
                return false;
            }
        }
        long requestSpecialTracking(UUID target,TrackingLevel level)
        {
            Set<Long> set = this.getOrCreateSpecialSet(target,level);
            long id = this.getNextID();
            set.add(id);
            this.keyToSpecialMap.put(id,target);
            return id;
        }
        void endSpecialTracking(UUID target,long key)
        {
            for(TrackingLevel level : TrackingLevel.HIGHEST_TO_LOWEST)
                this.getOrCreateSpecialSet(target,level).remove(key);
            //Remove from the key-map to make this key no longer flagged as "special"
            this.keyToSpecialMap.remove(key);
            //Remove the entire special tracking map if it's completely empty
            Map<TrackingLevel,Set<Long>> map = this.specialTrackingMap.get(target);
            if(map != null)
            {
                boolean empty = true;
                for(TrackingLevel level : TrackingLevel.HIGHEST_TO_LOWEST)
                {
                    if(!map.getOrDefault(level,new HashSet<>()).isEmpty())
                        empty = false;
                }
                if(empty)
                    this.specialTrackingMap.remove(target);
            }
        }
        Set<TraderNodeType<?>> cleanChangedNodes() {
            Set<TraderNodeType<?>> result = new HashSet<>(this.changedSinceLastPacket);
            this.changedSinceLastPacket.clear();
            return result;
        }
    }

    public record Result(long key, @Nullable Set<TraderNodeType<?>> changedNodes) {}

    private record Context(Player player,PlayerData data,@Nullable UUID specialRequest) implements ISyncingContext
    {
        @Override
        public UUID getPlayerID() { return this.player.getUUID(); }
        @Override
        public TrackingLevel getPlayerTrackingLevel() { return this.data.getHighestLevel(); }
        @Override
        public TrackingLevel getSpecialRequestLevel(UUID playerID) { return this.data.getHighestSpecialLevel(playerID); }
        @Override
        public Set<UUID> getSpecialCustomerSet() {
            if(this.specialRequest != null && this.getSpecialRequestLevel(this.specialRequest).isLevel(TrackingLevel.CUSTOMER))
                return Set.of(this.specialRequest);
            return new HashSet<>(this.data.specialTrackingMap.keySet());}

        @Nullable
        @Override
        public Pair<UUID, TrackingLevel> addedSpecialRequest() { return this.specialRequest == null ? null : Pair.of(this.specialRequest,this.data.getHighestSpecialLevel(this.specialRequest)); }
    }

}