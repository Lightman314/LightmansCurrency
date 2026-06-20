package io.github.lightman314.lightmanscurrency.api.trader.nodes;

import java.util.Map;
import java.util.Optional;

public interface NodeCollector {

    void addNode(TraderNodeType<?> node);
    void addNode(TraderNodeType<?> node,Optional<Object> argument);
    void removeNode(TraderNodeType<?> node);

    static NodeCollector forMap(Map<TraderNodeType<?>,Optional<Object>> map) { return forMap(map,TraderArguments.builder()); }
    static NodeCollector forMap(Map<TraderNodeType<?>,Optional<Object>> map,TraderArguments arguments) { return new Basic(map,arguments); }

    final class Basic implements NodeCollector
    {
        private final Map<TraderNodeType<?>,Optional<Object>> data;
        private final TraderArguments arguments;
        private Basic(Map<TraderNodeType<?>,Optional<Object>> map,TraderArguments arguments) { this.data = map; this.arguments = arguments; }

        @Override
        public void addNode(TraderNodeType<?> node) { this.data.put(node,this.arguments.get(node)); }
        @Override
        public void addNode(TraderNodeType<?> node,Optional<Object> argument) { this.data.put(node,argument); }
        @Override
        public void removeNode(TraderNodeType<?> node) { this.data.remove(node); }
    }

}