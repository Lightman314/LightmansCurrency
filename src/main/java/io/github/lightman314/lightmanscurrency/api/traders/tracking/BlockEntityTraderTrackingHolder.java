package io.github.lightman314.lightmanscurrency.api.traders.tracking;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Used to track trader tracking based on a singular block within the world, effectively tracking a singular trader across multiple players<br>
 * Has utility methods for if/when the trader being tracked by this block ceases to exist
 */
public final class BlockEntityTraderTrackingHolder {

    private final IClientTracker parent;
    private final Map<UUID,PlayerTraderTrackingHolder> trackingCache = new HashMap<>();
    public BlockEntityTraderTrackingHolder(IClientTracker parent) { this.parent = parent; }

    @Nullable
    private PlayerTraderTrackingHolder getHolder(UUID player) { return this.trackingCache.get(player); }

    private PlayerTraderTrackingHolder getOrMakeHolder(Player player) {
        if(!this.trackingCache.containsKey(player.getUUID()))
            this.trackingCache.put(player.getUUID(),new PlayerTraderTrackingHolder(this.parent,TrackingLevel.CUSTOMER));
        return this.trackingCache.get(player.getUUID());
    }

    @Nullable
    private Player getPlayer(UUID id) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null)
            return server.getPlayerList().getPlayer(id);
        return null;
    }

    public void requestTracking(TraderData trader,Player player)
    {
        if(this.parent.isClient())
            return;
        PlayerTraderTrackingHolder holder = this.getOrMakeHolder(player);
        holder.requestTracking(trader,player);
    }

    public void endTracking(long traderID)
    {
        if(this.parent.isClient())
            return;
        for(UUID playerID : new ArrayList<>(this.trackingCache.keySet()))
        {
            Player player = this.getPlayer(playerID);
            if(player != null)
            {
                PlayerTraderTrackingHolder holder = this.getHolder(playerID);
                if(holder != null)
                    holder.endTracking(traderID,player);
            }
            else
                this.clearPlayer(playerID);
        }
    }

    public void clearAll() {
        if(this.parent.isClient())
            return;
        for(UUID playerID : new ArrayList<>(this.trackingCache.keySet()))
            this.clearPlayer(playerID);
    }

    public void clearPlayer(Player player) {
        if(this.parent.isClient())
            return;
        PlayerTraderTrackingHolder holder = this.getHolder(player.getUUID());
        if(holder != null)
            holder.clear(player);
        this.trackingCache.remove(player.getUUID());
    }

    public void clearPlayer(UUID playerID) {
        if(this.parent.isClient())
            return;
        Player player = this.getPlayer(playerID);
        if(player != null)
        {
            this.clearPlayer(player);
            return;
        }
        //Otherwise force the players tracking to be cleared
        PlayerTraderTrackingHolder holder = this.getHolder(playerID);
        if(holder != null)
            holder.clear(playerID);
        this.trackingCache.remove(playerID);
    }

}
