package io.github.lightman314.lightmanscurrency.api.trader.nodes;

import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;

public class TraderNodeGroup<T extends TraderNode> {

    private final Set<TraderNodeType<? extends T>> types = new HashSet<>();
    public Set<TraderNodeType<? extends T>> getTypes() { return ImmutableSet.copyOf(this.types); }

    @SafeVarargs
    public TraderNodeGroup(TraderNodeType<? extends T>... types) { this.types.addAll(ImmutableSet.copyOf(types)); }

    public void registerExtraType(TraderNodeType<? extends T> type) { this.types.add(type); }

}