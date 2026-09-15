package io.github.lightman314.lightmanscurrency.api.trader.nodes;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class TraderArguments {

    private final Map<Identifier,Object> map = new HashMap<>();
    private TraderArguments() {}

    public static TraderArguments builder() { return new TraderArguments(); }

    public static TraderArguments single(TraderNodeType<?> type,Object arg) { return builder().with(type,arg); }

    public TraderArguments with(TraderNodeType<?> type,Object argument) { return this.with(type.getKey(),argument); }
    public TraderArguments with(Identifier key,Object argument) {
        this.map.put(key,argument);
        return this;
    }

    public Optional<Object> get(TraderNodeType<?> type) { return this.get(type.getKey()); }
    public Optional<Object> get(Identifier type) { return Optional.ofNullable(this.map.get(type)); }
    public <T> Optional<T> tryGet(TraderNodeType<?> type,Class<T> clazz) { return tryGet(type.getKey(),clazz); }
    public <T> Optional<T> tryGet(Identifier type,Class<T> clazz) {
        Optional<Object> optional = this.get(type);
        if(optional.isPresent())
        {
            Object val = optional.get();
            if(clazz.isInstance(val))
                return Optional.of(clazz.cast(val));
        }
        return Optional.empty();
    }

}