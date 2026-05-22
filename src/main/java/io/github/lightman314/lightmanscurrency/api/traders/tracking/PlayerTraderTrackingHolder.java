package io.github.lightman314.lightmanscurrency.api.traders.tracking;

import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class to be stored by any object that can trigger trader tracking for multiple traders<br>
 * Assumes only one {@link Player} is tracking the traders despite the Player argument (which is mostly used to avoid storing a Player entity locally)
 */
public final class PlayerTraderTrackingHolder {

    private final Map<Long,Long> trackingCache = new HashMap<>();

    private final IClientTracker parent;
    private final TrackingLevel level;
    public PlayerTraderTrackingHolder(IClientTracker parent,TrackingLevel level) {
        this.parent = parent;
        this.level = level;
    }

    public void clear(Player player) {
        if(this.parent.isClient())
            return;
        for(long traderID : new ArrayList<>(this.trackingCache.keySet()))
            this.endTracking(traderID,player);
    }
    public void clear(UUID playerID) {
        if(this.parent.isClient())
            return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null)
        {
            Player player = server.getPlayerList().getPlayer(playerID);
            if(player != null)
            {
                this.clear(player);
                return;
            }
        }
        this.forceEndTracking(playerID);
    }

    public void updateTracking(long oldID,long newID,Player player) {
        if(this.parent.isClient())
            return;
        if(this.trackingCache.containsKey(oldID))
            this.endTracking(oldID,player);
        TraderData newTrader = TraderAPI.getApi().GetTrader(false,newID);
        if(newTrader != null)
            this.requestTracking(newTrader,player);
    }

    public void requestTracking(TraderData trader, Player player) {
        if(this.parent.isClient())
            return;
        //Ignore if we are already tracking that trader
        if(this.trackingCache.containsKey(trader.getID()))
            return;
        this.trackingCache.put(trader.getID(),trader.requestTracking(player,this.level));
    }

    public void endTracking(long traderID,Player player) {
        if(this.parent.isClient())
            return;
        TraderData trader = TraderAPI.getApi().GetTrader(false,traderID);
        if(trader != null)
            this.endTracking(trader,player);
        else
        {
            //If the trader no longer exists, remove the key from storage
            this.trackingCache.remove(traderID);
        }
    }
    public void endTracking(TraderData trader,Player player) {
        if(this.parent.isClient())
            return;
        if(this.trackingCache.containsKey(trader.getID()))
            trader.endTracking(player,this.trackingCache.remove(trader.getID()));
    }
    private void forceEndTracking(UUID playerID)
    {
        if(this.parent.isClient())
            return;
        this.trackingCache.clear();
    }

}
