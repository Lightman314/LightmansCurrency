package io.github.lightman314.lightmanscurrency.api.traders.data.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class TraderNodeType<T extends TraderNode> {

    public static final Codec<TraderNodeType<?>> CODEC = LCRegistries.TRADER_NODE.byNameCodec();

    public abstract T create(@Nullable Object argument);

    public abstract MapCodec<T> codec();

    @Override
    public int hashCode() { return LCRegistries.TRADER_NODE.getKey(this).hashCode(); }
    @Override
    public String toString() { return "TraderNodeType[" + LCRegistries.TRADER_NODE.getKey(this) + "]"; }

    public static <T extends TraderNode> TraderNodeType<T> simple(Supplier<T> factory, MapCodec<T> codec) { return advanced(o -> factory.get(),codec); }
    public static <T extends TraderNode> TraderNodeType<T> advanced(Function<Object,T> factory,MapCodec<T> codec) { return new SimpleType<>(factory,codec); }
    public static <T extends TraderNode> TraderNodeType<T> unit(Supplier<T> factory) { return new UnitType<>(factory); }

    private static class UnitType<T extends TraderNode> extends TraderNodeType<T>
    {
        private final Supplier<T> factory;
        private final MapCodec<T> codec;
        private UnitType(Supplier<T> factory) {
            this.factory = factory;
            this.codec = MapCodec.unit(this.factory);
        }
        @Override
        public T create(@Nullable Object argument) { return this.factory.get(); }
        @Override
        public MapCodec<T> codec() { return this.codec; }
    }

    private static class SimpleType<T extends TraderNode> extends TraderNodeType<T>
    {
        private final Function<Object,T> factory;
        private final MapCodec<T> codec;
        private SimpleType(Function<Object,T> factory,MapCodec<T> codec) {
            this.factory = factory;
            this.codec = codec;
        }
        @Override
        public T create(@Nullable Object argument) { return this.factory.apply(argument); }
        @Override
        public MapCodec<T> codec() { return this.codec; }
    }

}
