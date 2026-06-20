package io.github.lightman314.lightmanscurrency.api.trader.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.RegistryHelper;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class TraderNodeType<T extends TraderNode> {

    public abstract T create(Optional<Object> argument);
    public final Codec<T> codec() { return this.mapCodec().codec(); }
    public abstract MapCodec<T> mapCodec();

    @Override
    public int hashCode() { return RegistryHelper.hash(LCRegistries.Trader.TRADER_NODE_TYPE,this); }
    @Override
    public String toString() { return RegistryHelper.toString("TraderNodeType",LCRegistries.Trader.TRADER_NODE_TYPE,this); }

    public static <T extends TraderNode> TraderNodeType<T> simple(Supplier<T> factory,MapCodec<T> codec) { return new SimpleType<>(factory,codec); }
    public static <T extends TraderNode> TraderNodeType<T> unit(Supplier<T> factory) { return new UnitType<>(factory); }

    private static class UnitType<T extends TraderNode> extends TraderNodeType<T>
    {
        private final Supplier<T> factory;
        private final MapCodec<T> codec;
        private UnitType(Supplier<T> factory) {
            this.factory = factory;
            this.codec = MapCodec.unit(factory);
        }
        @Override
        public T create(Optional<Object> argument) { return this.factory.get(); }
        @Override
        public MapCodec<T> mapCodec() { return this.codec; }
    }

    private static class SimpleType<T extends TraderNode> extends TraderNodeType<T>
    {
        private final Supplier<T> factory;
        private final MapCodec<T> codec;
        private SimpleType(Supplier<T> factory,MapCodec<T> codec) {
            this.factory = factory;
            this.codec = codec;
        }
        @Override
        public T create(Optional<Object> argument) {
            T result = this.factory.get();
            result.updateArgument(argument);
            return result;
        }
        @Override
        public MapCodec<T> mapCodec() { return this.codec; }
    }

}