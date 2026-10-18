package io.github.lightman314.lightmanscurrency.features.api_impl.data;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.data.FancyData;
import io.github.lightman314.lightmanscurrency.api.data.FancyDataType;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.tracking.UUIDTrackingSet;
import io.github.lightman314.lightmanscurrency.api.stats.StatisticsDataHolder;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatHolder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class PlayerStatsDataCache extends FancyData {

    private static final Codec<PlayerStatsDataCache> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC,StatisticsDataHolder.CODEC)
            .xmap(PlayerStatsDataCache::new,d -> d.data);

    public static final FancyDataType<PlayerStatsDataCache> TYPE = new FancyDataType<>("player_stats",PlayerStatsDataCache::new,CODEC);

    private final Map<UUID,StatisticsDataHolder> data = new HashMap<>();
    private final UUIDTrackingSet tracking = new UUIDTrackingSet();

    private Map<UUID,FancyPacketMap.Mutable> changedData = new HashMap<>();

    private PlayerStatsDataCache() {}
    private PlayerStatsDataCache(Map<UUID,StatisticsDataHolder> data) {
        data.forEach((id,entry) -> this.data.put(id,entry.withListener(buildListener(id))));
    }

    private FancyPacketMap.Listener buildListener(UUID playerID) {
        return c -> this.changedData.computeIfAbsent(playerID,i -> FancyPacketMap.map());
    }

    private StatisticsDataHolder getOrCreate(UUID id) {
        if(!this.data.containsKey(id))
            this.data.put(id,new StatisticsDataHolder().withListener(buildListener(id)));
        return this.data.get(id);
    }

    public StatHolder getOrCreatePlayerStats(PlayerReference player) { return this.getOrCreate(player.id); }

    public long requestStatTracking(PlayerReference target,Player player) {
        var result = this.tracking.requestTracking(target.id,player);
        if(result.sendFreshPacket())
            this.sendInitialPacket(player,target.id);
        return result.trackingKey();
    }

    public void endStatTracking(long trackingKey) {
        this.tracking.endTracking(trackingKey);
    }

    @Override
    public FancyDataType<?> getType() { return TYPE; }

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        //Unlike notifications, only send the players stats when they're explicitly asking for them
    }

    @Override
    public void onPlayerLeave(ServerPlayer player) {
        super.onPlayerLeave(player);
    }

    private void sendInitialPacket(Player player, UUID id) {
        StatisticsDataHolder data = this.getOrCreate(player.getUUID());
        FancyPacketMap.Mutable update = FancyPacketMap.map();
        data.writePacket(update);
        this.sendPacket(player,this.assembleUpdatePacket(id,update));
    }

    private FancyPacketMap assembleUpdatePacket(UUID id,FancyPacketMap update) {
        return FancyPacketMap.map()
                .setUUID("player",id)
                .setMap("data",update);
    }

    @Override
    public void syncTick() {
        if(this.changedData.isEmpty())
            return;
        Map<UUID,FancyPacketMap.Mutable> updates = this.changedData;
        this.changedData = new HashMap<>();
        updates.forEach((id,packet) -> {
            FancyPacketMap update = this.assembleUpdatePacket(id,packet);
            for(UUID tracking : this.tracking.getTrackingPlayers(id))
                this.sendPacketToTarget(tracking,update);
        });
    }

    @Override
    protected void handleSyncPacket(FancyPacketMap data) {
        if(data.contains("player") && data.contains("data")) {
            UUID id = data.getUUID("player");
            if(id == null)
                return;
            this.getOrCreate(id).handlePacket(data.getMap("data"));
        }
    }

}
