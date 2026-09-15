package io.github.lightman314.lightmanscurrency.api.trader.nodes;

import com.google.common.collect.ImmutableSet;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.resources.Identifier;

import java.util.*;

public interface NodeCollector extends Iterable<TraderNodeType<?>> {

    void addNode(TraderNodeType<?> node);
    default void addNode(TraderNodeType<?> node,Object argument) { this.addNode(node); this.addArgument(node,argument); }
    default void addArgument(TraderNodeType<?> node,Object argument) { this.addArgument(LCRegistries.Trader.TRADER_NODE_TYPE.getKey(node),argument); }
    void addArgument(Identifier key,Object argument);
    void removeNode(TraderNodeType<?> node);
    default void replaceNode(TraderNodeType<?> oldNode,TraderNodeType<?> newNode) {
        this.removeNode(oldNode);
        this.addNode(newNode);
    }

    static NodeCollector basic() { return basic(TraderArguments.builder()); }
    static NodeCollector basic(TraderArguments arguments) { return new Basic(arguments); }

    final class Basic implements NodeCollector
    {
        private final Set<TraderNodeType<?>> nodes = new HashSet<>();
        private final TraderArguments arguments;
        private Basic(TraderArguments arguments) { this.arguments = arguments; }

        @Override
        public void addNode(TraderNodeType<?> node) { this.nodes.add(node); }
        @Override
        public void addArgument(Identifier key,Object argument) { this.arguments.with(key,argument); }
        @Override
        public void removeNode(TraderNodeType<?> node) { this.nodes.remove(node); }
        @Override
        public Iterator<TraderNodeType<?>> iterator() { return ImmutableSet.copyOf(this.nodes).iterator(); }
    }

}