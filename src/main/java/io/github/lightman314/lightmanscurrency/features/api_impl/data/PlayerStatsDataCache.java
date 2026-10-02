package io.github.lightman314.lightmanscurrency.features.api_impl.data;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.data.FancyData;
import io.github.lightman314.lightmanscurrency.api.data.FancyDataType;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.stats.StatisticsDataHolder;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatHolder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.*;

public class PlayerStatsDataCache extends FancyData {

    private static final Codec<PlayerStatsDataCache> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC,StatisticsDataHolder.CODEC)
            .xmap(PlayerStatsDataCache::new,d -> d.data);

    public static final FancyDataType<PlayerStatsDataCache> TYPE = new FancyDataType<>("player_stats",PlayerStatsDataCache::new,CODEC);

    private final Map<UUID,StatisticsDataHolder> data = new HashMap<>();

    private Map<UUID,FancyPacketMap.Mutable> changedData = new HashMap<>();

    private final Map<UUID,Set<UUID>> statTracking = new HashMap<>();

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

    public void requestStatTracking(PlayerReference target,Player player) {
        Set<UUID> tracking = this.statTracking.computeIfAbsent(target.id,i -> new HashSet<>());
        tracking.add(player.getUUID());
        this.sendInitialPacket(player,target.id);
    }

    public void endStatTracking(PlayerReference target,Player player) {
        Set<UUID> tracking = this.statTracking.get(target.id);
        //Cannot cancel the tracking of your own personal stats
        if(tracking == null || target.is(player))
            return;
        tracking.remove(player.getUUID());
        if(tracking.isEmpty())
            this.statTracking.remove(target.id);
    }

    @Override
    public FancyDataType<?> getType() { return TYPE; }

    @Override
    public void onPlayerJoin(ServerPlayer player) {
        this.requestStatTracking(PlayerReference.of(player),player);
    }

    private void sendInitialPacket(Player player,UUID id) {
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

    private void sendUpdateToTracking(UUID id,FancyPacketMap update) {
        Set<UUID> tracking = this.statTracking.get(id);
        if(tracking == null)
            return;
        MinecraftServer server = this.getServer();
        if(server != null) {
            FancyPacketMap fullPacket = this.assembleUpdatePacket(id,update);
            for(UUID p : Set.copyOf(tracking)) {
                Player player = server.getPlayerList().getPlayer(p);
                if(player == null)
                    tracking.remove(p);
                else
                    this.sendPacket(player,fullPacket);
            }
            if(tracking.isEmpty())
                this.statTracking.remove(id);
        }
    }

    @Override
    public void syncTick() {
        if(this.changedData.isEmpty())
            return;
        Map<UUID,FancyPacketMap.Mutable> updates = this.changedData;
        this.changedData = new HashMap<>();
        updates.forEach(this::sendUpdateToTracking);
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

    @Override
    protected void setupServer() {
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public void onServerShutdown() {
        NeoForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    private void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        //Clear all tracking from the player when they leave
        Player player = event.getEntity();
        for(UUID entry : Set.copyOf(this.statTracking.keySet()))
            this.endStatTracking(PlayerReference.of(entry,""),player);
    }

}
