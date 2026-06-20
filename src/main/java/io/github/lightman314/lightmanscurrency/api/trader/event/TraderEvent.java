package io.github.lightman314.lightmanscurrency.api.trader.event;

import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import net.neoforged.bus.api.Event;

import java.util.Optional;

public abstract class TraderEvent extends Event {

    public abstract long getTraderID();
    public abstract TraderData getTrader();

    protected static class Direct extends TraderEvent
    {
        private final TraderData trader;

        @Override
        public final long getTraderID() { return this.trader.getID(); }
        @Override
        public final TraderData getTrader() { return this.trader; }

        public Direct(TraderData trader) { this.trader = trader; }
    }

    public static class RegisterNodesEvent extends Direct implements NodeCollector
    {
        private final NodeCollector collector;
        public RegisterNodesEvent(TraderData trader,NodeCollector collector) { super(trader); this.collector = collector; }
        @Override
        public void addNode(TraderNodeType<?> node) { this.collector.addNode(node); }
        @Override
        public void addNode(TraderNodeType<?> node,Optional<Object> argument) { this.collector.addNode(node,argument); }
        @Override
        public void removeNode(TraderNodeType<?> node) { this.collector.removeNode(node); }
    }

}