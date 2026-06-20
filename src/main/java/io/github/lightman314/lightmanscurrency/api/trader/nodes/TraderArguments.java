package io.github.lightman314.lightmanscurrency.api.trader.nodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class TraderArguments {

    private final Map<TraderNodeType<?>,Object> map = new HashMap<>();
    private TraderArguments() {}

    public static TraderArguments builder() { return new TraderArguments(); }

    public static TraderArguments single(TraderNodeType<?> type,Object arg) { return builder().with(type,arg); }

    public TraderArguments with(TraderNodeType<?> type,Object argument) {
        this.map.put(type,argument);
        return this;
    }
    public TraderArguments with(TraderNodeGroup<?> group,Object argument) {
        for(TraderNodeType<?> type : group.getTypes())
            this.map.put(type,argument);
        return this;
    }

    public Optional<Object> get(TraderNodeType<?> type) { return Optional.ofNullable(this.map.get(type)); }

}