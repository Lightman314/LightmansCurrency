package io.github.lightman314.lightmanscurrency.api.trader.tracking;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerTrackingHolder implements ISidedContext{

    //Map of trader ID -> tracking keys
    private final Map<Long,Long> trackingCache = new HashMap<>();

    private final ISidedContext parent;
    private final TrackingLevel level;
    public PlayerTrackingHolder(ISidedContext parent, TrackingLevel level) {
        this.parent = parent;
        this.level = level;
    }

    @Override
    public boolean isClient() { return this.parent.isClient(); }

    public void clear(Player player)
    {
        if(this.isClient())
            return;
        for(long traderID : new ArrayList<>(this.trackingCache.keySet()))
            this.endTracking(traderID,player);
    }
    public void clear(UUID playerID) {
        if(this.isClient())
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
        if(this.isClient())
            return;
        if(this.trackingCache.containsKey(oldID))
            this.endTracking(oldID,player);
        TraderData newTrader = LCApi.getTraderAPI().getTrader(this,newID);
        if(newTrader != null)
            this.requestTracking(newTrader,player);
    }

    public void requestTracking(TraderData trader,Player player) {
        if(this.isClient())
            return;
        //Ignore if we're already tracking that trader
        if(this.trackingCache.containsKey(trader.getID()))
            return;
        this.trackingCache.put(trader.getID(),trader.requestTracking(player,this.level));
    }

    public void endTracking(long traderID,Player player) {
        if(this.isClient())
            return;
        TraderData trader = LCApi.getTraderAPI().getTrader(this,traderID);
        if(trader != null)
            this.endTracking(trader,player);
        else
        {
            //If the trader no longer exists, remove the key from the cache
            this.trackingCache.remove(traderID);
        }
    }
    public void endTracking(TraderData trader,Player player) {
        if(this.parent.isClient())
            return;
        if(this.trackingCache.containsKey(trader.getID()))
            trader.endTracking(player,this.trackingCache.remove(trader.getID()));
    }
    public void forceEndTracking(UUID playerID) {
        if(this.isClient())
            return;
        this.trackingCache.clear();
    }

}