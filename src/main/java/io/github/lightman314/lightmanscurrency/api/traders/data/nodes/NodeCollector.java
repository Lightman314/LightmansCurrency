package io.github.lightman314.lightmanscurrency.api.traders.data.nodes;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public interface NodeCollector {

    void addNode(TraderNodeType<?> node);
    void addNode(TraderNodeType<?> node, @Nullable Object argument);
    void removeNode(TraderNodeType<?> node);

    static NodeCollector forMap(Map<TraderNodeType<?>,Object> map) { return forMap(map,new HashMap<>()); }
    static NodeCollector forMap(Map<TraderNodeType<?>,Object> map,Map<TraderNodeType<?>,Object> preloadedArgs) { return new Basic(map,preloadedArgs); }

    final class Basic implements NodeCollector
    {
        private final Map<TraderNodeType<?>,Object> data;
        private final Map<TraderNodeType<?>,Object> preloadedArgs;
        private Basic(Map<TraderNodeType<?>,Object> map,Map<TraderNodeType<?>,Object> args) { this.data = map; this.preloadedArgs = args; }

        @Override
        public void addNode(TraderNodeType<?> node) { this.data.put(node,this.preloadedArgs.get(node)); }
        @Override
        public void addNode(TraderNodeType<?> node,@Nullable Object argument) { this.data.put(node,argument); }
        @Override
        public void removeNode(TraderNodeType<?> node) { this.data.remove(node); }
    }

}
