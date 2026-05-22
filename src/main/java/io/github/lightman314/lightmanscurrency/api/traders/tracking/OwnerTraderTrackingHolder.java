package io.github.lightman314.lightmanscurrency.api.traders.tracking;

import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Class to be stored by any object that can trigger trader tracking on behalf of a third-part owner<br>
 * Assumes only one {@link Player} is tracking the traders despite the Player argument (which is mostly used to avoid storing a Player entity locally)<br>
 * The {@link OwnerData} supplier provided will be used to determine which pseudo-player to track the trader under
 */
public final class OwnerTraderTrackingHolder {

    private final Map<Long,TrackingData> trackingCache = new HashMap<>();

    private final IClientTracker parent;
    private final TrackingLevel level;
    public OwnerTraderTrackingHolder(IClientTracker parent,TrackingLevel level) {
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

    public void requestTracking(TraderData trader,Player player,OwnerData owner) { this.requestTracking(trader,player,owner.getValidOwner()); }
    public void requestTracking(TraderData trader,Player player,Owner owner) { this.requestTracking(trader,player,owner.asPlayerReference().id); }
    public void requestTracking(TraderData trader,Player player,UUID owner) {
        if(this.parent.isClient())
            return;
        if(owner.equals(player.getUUID()))
            owner = null;
        //Ignore if we are already tracking that trader
        if(this.trackingCache.containsKey(trader.getID()))
        {
            //Check if the UUID matches
            if(!Objects.equals(owner,this.trackingCache.get(trader.getID()).pseudoplayer))
            {
                this.endTracking(trader,player);
                this.startTracking(trader,player,owner);
            }
            return;
        }
        this.startTracking(trader,player,owner);
    }

    private void startTracking(TraderData trader, Player player, @Nullable UUID owner)
    {
        if(owner == null)
            this.trackingCache.put(trader.getID(),new TrackingData(trader.requestTracking(player,this.level),null));
        else
            this.trackingCache.put(trader.getID(),new TrackingData(trader.requestSpecialTracking(player,this.level,owner),owner));
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
            trader.endTracking(player,this.trackingCache.remove(trader.getID()).trackingKey);
    }
    private void forceEndTracking(UUID playerID)
    {
        if(this.parent.isClient())
            return;
        this.trackingCache.clear();
    }

    private record TrackingData(long trackingKey,@Nullable UUID pseudoplayer) { }

}
