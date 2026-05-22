package io.github.lightman314.lightmanscurrency.api.events;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

import javax.annotation.Nullable;
import java.util.Map;

public abstract class TraderEvent extends Event {
	
	private final long traderID;
	public final long getID() { return this.traderID; }
	public final OwnerData getOwner() { return this.getTrader() == null ? null : this.getTrader().getOwner(); }
	public TraderData getTrader() { return TraderAPI.getApi().GetTrader(false, this.traderID); }
	
	protected TraderEvent(long traderID) { this.traderID = traderID; }

    public static class RegisterNodesEvent extends TraderEvent implements NodeCollector
    {
        private final TraderData trader;
        private final Map<TraderNodeType<?>,Object> nodes;
        public Map<TraderNodeType<?>,Object> getNodes() { return this.nodes; }
        public RegisterNodesEvent(TraderData trader,Map<TraderNodeType<?>,Object> nodes)
        {
            super(trader.getID());
            this.trader = trader;
            this.nodes = nodes;
        }
        @Override
        public TraderData getTrader() { return this.trader; }

        @Override
        public void addNode(TraderNodeType<?> type) { this.addNode(type,null);}
        @Override
        public void addNode(TraderNodeType<?> type,@Nullable Object argument) { this.nodes.put(type,argument); LightmansCurrency.LogDebug("Added " + type + " as a trader node."); }
        @Override
        public void removeNode(TraderNodeType<?> type) { this.nodes.remove(type); }
    }

    public static class CreateNewTraderEvent extends TraderEvent
    {
        private final Player player;
        @Nullable
        public Player getPlayer() { return this.player; }

        public CreateNewTraderEvent(long traderID, @Nullable Player player)
        {
            super(traderID);
            this.player = player;
        }
    }

    public static class TraderDeletedEvent extends TraderEvent
    {
        private final TraderData trader;
        @Override
        public TraderData getTrader() { return this.trader; }
        public TraderDeletedEvent(long traderID, TraderData removedTrader) {
            super(traderID);
            this.trader = removedTrader;
        }
    }

    public static class TraderNetworkStatusUpdated extends TraderEvent
    {
        public boolean isNetworkAccessible() {
            TraderData trader = this.getTrader();
            return trader != null && trader.isNetworkAccessible();
        }
        public TraderNetworkStatusUpdated(long traderID) { super(traderID); }
    }
	
}
