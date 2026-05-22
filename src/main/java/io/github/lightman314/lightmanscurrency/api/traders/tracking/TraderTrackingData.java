package io.github.lightman314.lightmanscurrency.api.traders.tracking;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
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
    public Result requestSpecialTracking(Player player,TrackingLevel level,UUID target)
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
        long key = data.requestSpecialTracking(target,level);
        return new Result(key,null);
    }

    public ISyncingContext getContext(Player player)
    {
        return new Context(player,this.data.getOrDefault(player.getUUID(),new PlayerData()),null);
    }
    public ISyncingContext getSpecialContext(Player player,UUID target)
    {
        return new Context(player,this.data.getOrDefault(player.getUUID(),new PlayerData()),target);
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
            return new HashSet<>();
        return data.changedSinceLastPacket;
    }

    private class PlayerData
    {
        final Map<TrackingLevel,Set<Long>> trackingMap = new HashMap<>();
        final Map<Long,UUID> keyToSpecialMap = new HashMap<>();
        final Map<UUID,Map<TrackingLevel,Set<Long>>> specialTrackingMap = new HashMap<>();
        final Set<TraderNodeType<?>> changedSinceLastPacket = new HashSet<>();
        TrackingLevel getHighestLevel()
        {
            if(!this.getOrCreateSet(TrackingLevel.STORAGE).isEmpty())
                return TrackingLevel.STORAGE;
            if(!this.getOrCreateSet(TrackingLevel.CUSTOMER).isEmpty())
                return TrackingLevel.CUSTOMER;
            return TrackingLevel.NONE;
        }
        TrackingLevel getHighestSpecialLevel(UUID target)
        {
            if(!this.getOrCreateSpecialSet(target,TrackingLevel.STORAGE).isEmpty())
                return TrackingLevel.STORAGE;
            if(!this.getOrCreateSpecialSet(target,TrackingLevel.CUSTOMER).isEmpty())
                return TrackingLevel.CUSTOMER;
            return TrackingLevel.NONE;
        }
        Set<Long> getOrCreateSet(TrackingLevel level)
        {
            if(!this.trackingMap.containsKey(level))
                this.trackingMap.put(level,new HashSet<>());
            return this.trackingMap.get(level);
        }
        Set<Long> getOrCreateSpecialSet(UUID target,TrackingLevel level)
        {
            if(this.specialTrackingMap.containsKey(target))
                this.specialTrackingMap.put(target,new HashMap<>());
            Map<TrackingLevel,Set<Long>> map = this.specialTrackingMap.get(target);
            if(!map.containsKey(level))
                map.put(level,new HashSet<>());
            return map.get(level);
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
                this.getOrCreateSet(TrackingLevel.STORAGE).remove(key);
                this.getOrCreateSet(TrackingLevel.CUSTOMER).remove(key);
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
            this.getOrCreateSpecialSet(target,TrackingLevel.STORAGE).remove(key);
            this.getOrCreateSpecialSet(target,TrackingLevel.CUSTOMER).remove(key);
            //Remove from the key-map to make this key no-longer flagged as "special"
            this.keyToSpecialMap.remove(key);
            //Remove the entire special tracking map if it's completely empty
            Map<TrackingLevel,Set<Long>> map = this.specialTrackingMap.get(target);
            if(map != null)
            {
                if(map.getOrDefault(TrackingLevel.CUSTOMER,new HashSet<>()).isEmpty() &&
                        map.getOrDefault(TrackingLevel.STORAGE,new HashSet<>()).isEmpty())
                    this.specialTrackingMap.remove(target);
            }
        }
        Set<TraderNodeType<?>> cleanChangedNodes() {
            Set<TraderNodeType<?>> result = new HashSet<>(this.changedSinceLastPacket);
            this.changedSinceLastPacket.clear();
            return result;
        }
    }

    public record Result(long key,@Nullable Set<TraderNodeType<?>> changedNodes) { }

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
            return new HashSet<>(this.data.specialTrackingMap.keySet());
        }
        @Nullable
        @Override
        public Pair<UUID, TrackingLevel> addedSpecialRequest() { return this.specialRequest == null ? null : Pair.of(this.specialRequest,this.data.getHighestSpecialLevel(this.specialRequest)); }
    }

}
