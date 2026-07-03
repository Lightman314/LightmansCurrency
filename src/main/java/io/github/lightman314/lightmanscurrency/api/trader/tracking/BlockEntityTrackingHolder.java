package io.github.lightman314.lightmanscurrency.api.trader.tracking;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.world.blockentity.EasyBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.*;

@EventBusSubscriber
public final class BlockEntityTrackingHolder implements ISidedContext {

    private final ISidedContext parent;
    private final BlockEntity be;
    private final Map<UUID, PlayerTrackingHolder> trackingCache = new HashMap<>();
    private final TrackingLevel level;
    public BlockEntityTrackingHolder(EasyBlockEntity be) { this(be,be,TrackingLevel.CUSTOMER); }
    public BlockEntityTrackingHolder(ISidedContext parent,BlockEntity be) { this(parent,be,TrackingLevel.CUSTOMER); }
    public BlockEntityTrackingHolder(EasyBlockEntity be,TrackingLevel level) { this(be,be,level); }
    public BlockEntityTrackingHolder(ISidedContext parent,BlockEntity be,TrackingLevel level) { this.parent = parent; this.be = be; this.level = level; }

    @Override
    public boolean isClient() { return this.parent.isClient(); }

    @Nullable
    private PlayerTrackingHolder getHolder(UUID player) { return this.trackingCache.get(player); }
    private PlayerTrackingHolder getOrMakeHolder(Player player) {
        return this.trackingCache.computeIfAbsent(player.getUUID(),id -> new PlayerTrackingHolder(this.parent,this.level));
    }

    @Nullable
    private Player getPlayer(UUID id) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.getPlayerList().getPlayer(id);
    }

    public void requestTracking(@Nullable TraderData trader) {
        if(trader == null)
            return;
        for(Player player : getPlayersTrackingChunk(this.be))
            this.requestTracking(trader,player);
    }

    public void requestTracking(@Nullable TraderData trader, Player player) {
        if(trader == null)
            LightmansCurrency.LogWarning("Requested tracking for a missing trader!");
        if(this.isClient() || trader == null)
            return;
        PlayerTrackingHolder holder = this.getOrMakeHolder(player);
        holder.requestTracking(trader,player);
    }

    public void endTracking(long traderID) {
        if(this.isClient())
            return;
        for(UUID playerID : new ArrayList<>(this.trackingCache.keySet()))
        {
            Player player = this.getPlayer(playerID);
            if(player != null)
            {
                PlayerTrackingHolder holder = this.getHolder(playerID);
                if(holder != null)
                    holder.endTracking(traderID,player);
            }
            else
                this.clearPlayer(playerID);
        }
    }

    public void clearAll() {
        if(this.isClient())
            return;
        for(UUID playerID : new ArrayList<>(this.trackingCache.keySet()))
            this.clearPlayer(playerID);
    }

    public void clearPlayer(Player player) {
        if(this.isClient())
            return;
        PlayerTrackingHolder holder = this.getHolder(player.getUUID());
        if(holder != null)
            holder.clear(player);
        this.trackingCache.remove(player.getUUID());
    }
    public void clearPlayer(UUID playerID) {
        if(this.isClient())
            return;
        Player player = this.getPlayer(playerID);
        if(player != null)
        {
            this.clearPlayer(player);
            return;
        }
        //Otherwise force the players tracking to be cleared
        PlayerTrackingHolder holder = this.getHolder(playerID);
        if(holder != null)
            holder.clear(playerID);
        this.trackingCache.remove(playerID);
    }

    public interface ITraderTrackingBE {

        void startTrackingPlayer(Player player);
        void stopTrackingPlayer(Player player);

    }

    public static List<Player> getPlayersTrackingChunk(BlockEntity be) {
        List<Player> list = new ArrayList<>();
        if(be.getLevel() instanceof ServerLevel sl)
            list.addAll(sl.getChunkSource().chunkMap.getPlayers(ChunkPos.containing(be.getBlockPos()),false));
        return list;
    }

    @SubscribeEvent
    private static void trackBlockEntity(ChunkWatchEvent.Sent event)
    {
        Player player = event.getPlayer();
        for(BlockEntity be : new ArrayList<>(event.getChunk().getBlockEntities().values()))
        {
            if(be instanceof ITraderTrackingBE tracker)
            {
                LightmansCurrency.LogDebug("BE at " + be.getBlockPos() + " implements tracking BE interface, requesting tracking for " + player.getName().getString());
                tracker.startTrackingPlayer(player);
            }

        }
    }

    @SubscribeEvent
    private static void untrackBlockEntity(ChunkWatchEvent.UnWatch event)
    {
        ChunkPos pos = event.getPos();
        ServerLevel level = event.getLevel();
        if(level.hasChunk(pos.x(),pos.z()))
        {
            LevelChunk chunk = level.getChunk(pos.x(),pos.z());
            Player player = event.getPlayer();
            for(BlockEntity be : new ArrayList<>(chunk.getBlockEntities().values()))
            {
                if(be instanceof ITraderTrackingBE tracker)
                    tracker.stopTrackingPlayer(player);
            }
        }
    }

}