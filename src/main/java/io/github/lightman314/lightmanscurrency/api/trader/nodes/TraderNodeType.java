package io.github.lightman314.lightmanscurrency.api.trader.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;

import java.util.function.Supplier;

public abstract class TraderNodeType<T extends TraderNode> extends AbstractType.WithCodec<T,TraderNodeType<?>> {

    public TraderNodeType(MapCodec<T> codec) { super(codec); }

    public abstract T create(TraderArguments arguments);
    public final Codec<T> fullCodec() { return this.codec().codec(); }

    @Override
    protected final TraderNodeType<?> getEntry() { return super.getEntry(); }
    @Override
    protected final Registry<TraderNodeType<?>> getRegistry() { return LCRegistries.Trader.TRADER_NODE_TYPE; }
    @Override
    protected final String getName() { return "TraderNodeType"; }

    public static <T extends TraderNode> TraderNodeType<T> simple(Supplier<T> factory,MapCodec<T> codec) { return new SimpleType<>(factory,codec); }
    public static <T extends TraderNode> TraderNodeType<T> unit(Supplier<T> factory) { return new UnitType<>(factory); }

    private static class UnitType<T extends TraderNode> extends TraderNodeType<T>
    {
        private final Supplier<T> factory;
        private UnitType(Supplier<T> factory) {
            super(MapCodec.unit(factory));
            this.factory = factory;
        }
        @Override
        public T create(TraderArguments arguments) { return this.factory.get(); }
    }

    private static class SimpleType<T extends TraderNode> extends TraderNodeType<T>
    {
        private final Supplier<T> factory;
        private SimpleType(Supplier<T> factory,MapCodec<T> codec) {
            super(codec);
            this.factory = factory;
        }
        @Override
        public T create(TraderArguments arguments) {
            T result = this.factory.get();
            result.updateArgument(arguments);
            return result;
        }
    }

}