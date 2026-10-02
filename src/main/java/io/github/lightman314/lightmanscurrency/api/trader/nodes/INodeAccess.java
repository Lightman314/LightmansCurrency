package io.github.lightman314.lightmanscurrency.api.trader.nodes;

import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.stats.interfaces.StatListener;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IStatListeningNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.TradingNode;
import net.minecraft.tags.TagKey;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface INodeAccess extends StatListener {

    @Nullable
    TraderData getTrader();

    default boolean hasNode(TraderNodeType<?> type) { return this.getNode(type) != null; }
    default boolean hasNode(Class<? extends TraderNode> type) { return this.getAllNodes().stream().anyMatch(type::isInstance); }
    default boolean hasNode(TagKey<TraderNodeType<?>> tag) { return this.getAllNodes().stream().anyMatch(n -> n.is(tag)); }
    @Nullable
    <T extends TraderNode> T getNode(TraderNodeType<T> type);
    default <T extends TraderNode> void ifNodePresent(TraderNodeType<T> type,Consumer<T> action) {
        T node = this.getNode(type);
        if(node != null)
            action.accept(node);
    }
    @Nullable
    default <T extends TraderNode,R> R getNodeValue(TraderNodeType<T> type, Function<T,R> getter) { return this.getNodeValue(type,getter,null); }
    default <T extends TraderNode,R> R getNodeValue(TraderNodeType<T> type, Function<T,R> getter,R defaultValue) {
        T node = this.getNode(type);
        if(node != null)
            return getter.apply(node);
        return defaultValue;
    }

    @Nullable
    default <T extends TraderNode,R,A> R getNodeArgValue(TraderNodeType<T> type,A arg, BiFunction<T,A,R> getter) { return this.getNodeArgValue(type,arg,getter,null); }
    default <T extends TraderNode,R,A> R getNodeArgValue(TraderNodeType<T> type,A arg,BiFunction<T,A,R> getter,R defaultValue) {
        T node = this.getNode(type);
        if(node != null)
            return getter.apply(node,arg);
        return defaultValue;
    }

    List<TraderNode> getAllNodes();
    default <T> List<T> getNodes(Class<T> subclass)
    {
        List<T> result = new ArrayList<>();
        for(TraderNode n : this.getAllNodes())
        {
            if(subclass.isInstance(n))
                result.add(subclass.cast(n));
        }
        return result;
    }
    @Nullable
    default <T> T getFirstNode(Class<T> subclass)
    {
        List<T> allNodes = this.getNodes(subclass);
        return allNodes.isEmpty() ? null : allNodes.getFirst();
    }
    default List<TraderNode> getNodes(TagKey<TraderNodeType<?>> tag) {
        List<TraderNode> result = new ArrayList<>();
        for(TraderNode n : this.getAllNodes()) {
            if(n.getType().is(tag))
                result.add(n);
        }
        return result;
    }
    default List<TradingNode<?>> getTradingNodes() {
        List<TradingNode<?>> list = new ArrayList<>();
        for(TraderNode node : this.getAllNodes())
        {
            if(node instanceof TradingNode<?> tn)
                list.add(tn);
        }
        //Sort them so that they'll always be in the same order
        list.sort(Comparator.comparingInt(TradingNode::getPriority));
        return list;
    }
    default List<TraderNode> getNodes(Predicate<TraderNode> filter) { return new ArrayList<>(this.getAllNodes().stream().filter(filter).toList()); }

    @Override
    default <V, T> void addToStat(StatKey<V, T> key, T addValue) {
        for(IStatListeningNode node : this.getNodes(IStatListeningNode.class))
            node.afterStatAdded(key,addValue);
    }

}