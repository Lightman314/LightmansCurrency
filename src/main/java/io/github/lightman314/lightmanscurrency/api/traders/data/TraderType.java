package io.github.lightman314.lightmanscurrency.api.traders.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class TraderType<T extends TraderData> {

    public static final Codec<TraderType<?>> CODEC = LCRegistries.TRADER_TYPES.byNameCodec();

    public abstract T create();
    public abstract MapCodec<T> mapCodec();

    @Override
    public final int hashCode() { return LCRegistries.TRADER_TYPES.getKey(this).hashCode(); }
    @Override
    public final String toString() { return "TraderType[" + LCRegistries.TRADER_TYPES.getKey(this) + "]"; }

    public static <T extends TraderData> TraderType<T> simple(Supplier<T> builder,BiFunction<Long,Map<TraderNodeType<?>, TraderNode>,T> factory) { return new Simple<>(builder,factory); }

    private static class Simple<T extends TraderData> extends TraderType<T>
    {
        private final Supplier<T> builder;
        private final MapCodec<T> codec;
        private Simple(Supplier<T> factory,BiFunction<Long,Map<TraderNodeType<?>, TraderNode>,T> decoder)
        {
            this.builder = factory;
            this.codec = RecordCodecBuilder.mapCodec(builder -> TraderData.baseFields(builder)
                    .apply(builder,decoder));
        }
        @Override
        public T create() { return this.builder.get(); }
        @Override
        public MapCodec<T> mapCodec() { return this.codec; }
    }

}
