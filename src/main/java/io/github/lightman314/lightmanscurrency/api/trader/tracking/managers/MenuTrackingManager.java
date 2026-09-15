package io.github.lightman314.lightmanscurrency.api.trader.tracking.managers;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderSource;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.TrackingLevel;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MenuTrackingManager implements ISidedContext {

    private final TraderSource traders;
    private final Player player;
    private final TrackingLevel level;
    private final Map<Long,Long> currentlyTracking = new HashMap<>();
    public MenuTrackingManager(TraderSource traders, Player player, TrackingLevel level) {
        this.traders = traders;
        this.player = player;
        this.level = level;
        if(this.isServer())
            this.tick();
    }

    @Override
    public boolean isClient() { return this.player.level().isClientSide(); }

    public void tick() {
        Set<Long> wasTracking = new HashSet<>(this.currentlyTracking.keySet());
        for(TraderData trader : this.traders.getTraders()) {
            long traderID = trader.getID();
            //If we're already tracking, remove from the temp set as we don't need to end its tracking
            if(wasTracking.contains(traderID))
                wasTracking.remove(traderID);
            else //Otherwise initialize the traders tracking
                this.currentlyTracking.put(traderID,trader.requestTracking(this.player,this.level));
        }
        for(long traderID : wasTracking)
            this.endTracking(traderID);
    }

    public void onClose() {
        for(long traderID : new HashSet<>(this.currentlyTracking.keySet()))
            this.endTracking(traderID);
    }

    private void endTracking(long traderID) {
        if(this.currentlyTracking.containsKey(traderID)) {
            long key = this.currentlyTracking.get(traderID);
            TraderData trader = LCApi.getTraderAPI().getTrader(this,traderID);
            if(trader != null)
                trader.endTracking(this.player,key);
            this.currentlyTracking.remove(traderID);
        }
    }
}