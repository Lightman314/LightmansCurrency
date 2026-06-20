package io.github.lightman314.lightmanscurrency.api.codecs;

import io.github.lightman314.lightmanscurrency.api.codecs.partial.*;

import com.mojang.datafixers.util.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class StreamHelper {

    private StreamHelper() {}

    public static final StreamCodec<RegistryFriendlyByteBuf,Optional<Item>> OPTIONAL_ITEM_STREAM = ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.ITEM));

    public static final StreamCodec<ByteBuf,BigDecimal> BIG_DECIMAL = ByteBufCodecs.STRING_UTF8.map(BigDecimal::new,BigDecimal::toString);

    public static <T> StreamCodec<FriendlyByteBuf,T> mapBufFriendly(StreamCodec<ByteBuf,T> codec) { return codec.mapStream(Function.identity()); }
    public static <T> StreamCodec<RegistryFriendlyByteBuf,T> mapBufReg(StreamCodec<ByteBuf,T> codec) { return codec.mapStream(Function.identity()); }
    public static <T> StreamCodec<RegistryFriendlyByteBuf,T> mapFriendReg(StreamCodec<FriendlyByteBuf,T> codec) { return codec.mapStream(Function.identity()); }

    public static <B,T> StreamCodec<B,T> uncheckedUnit(T instance) { return uncheckedUnit(() -> instance); }
    public static <B,T> StreamCodec<B,T> uncheckedUnit(Supplier<T> supplier)
    {
        return StreamCodec.of(
                (buffer, value) -> {},
                buffer -> supplier.get());
    }

    public static <T> StreamCodec<ByteBuf,T> byNameCodec(Function<Identifier,T> valueGetter,Function<T,Identifier> idGetter)
    {
        return Identifier.STREAM_CODEC.map(valueGetter,idGetter);
    }

    public static <B,T1,T2> StreamCodec<B,Pair<T1,T2>> pair(StreamCodec<? super B,T1> codec1,StreamCodec<? super B,T2> codec2)
    {
        return StreamCodec.composite(codec1,Pair::getFirst,codec2,Pair::getSecond,Pair::of);
    }

    public static <B extends ByteBuf,T> StreamCodec<B,Set<T>> setCodec(StreamCodec<B,T> codec)
    {
        return codec.apply(ByteBufCodecs.list()).map(HashSet::new,ArrayList::new);
    }

    public static <B extends ByteBuf,K,V> StreamCodec<B, Map<K,V>> unboundedMap(StreamCodec<? super B,K> keyCodec, StreamCodec<? super B,V> valueCodec)
    {
        return StreamCodec.of((buf,map) -> {
            buf.writeInt(map.size());
            map.forEach((key,value) -> {
                keyCodec.encode(buf,key);
                valueCodec.encode(buf,value);
            });
        },(buf) -> {
            Map<K,V> result = new HashMap<>();
            int count = buf.readInt();
            for(int i = 0; i < count; ++i)
            {
                K key = keyCodec.decode(buf);
                V value = valueCodec.decode(buf);
                result.put(key,value);
            }
            return result;
        });
    }

    /// Expanded StreamCodec#composite methods for up to 12 arguments
    public static <B,C,T1,T2,T3,T4,T5,T6,T7> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7,
            final Function7<T1, T2, T3, T4, T5, T6, T7, C> factory)
    {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                return factory.apply(t1,t2,t3,t4,t5,t6,t7);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
                codec7.encode(buffer, getter7.apply(value));
            }
        };
    }

    public static <B,C,T1,T2,T3,T4,T5,T6,T7,T8> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7,
            final StreamCodec<? super B, T8> codec8, final Function<C, T8> getter8,
            final Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                return factory.apply(t1,t2,t3,t4,t5,t6,t7,t8);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
                codec7.encode(buffer, getter7.apply(value));
                codec8.encode(buffer, getter8.apply(value));
            }
        };
    }

    public static <B,C,T1,T2,T3,T4,T5,T6,T7,T8,T9> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7,
            final StreamCodec<? super B, T8> codec8, final Function<C, T8> getter8,
            final StreamCodec<? super B, T9> codec9, final Function<C, T9> getter9,
            final Function9<T1,T2,T3,T4,T5,T6,T7,T8,T9,C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                return factory.apply(t1,t2,t3,t4,t5,t6,t7,t8,t9);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
                codec7.encode(buffer, getter7.apply(value));
                codec8.encode(buffer, getter8.apply(value));
                codec9.encode(buffer, getter9.apply(value));
            }
        };
    }

    public static <B,C,T1,T2,T3,T4,T5,T6,T7,T8,T9,TA> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7,
            final StreamCodec<? super B, T8> codec8, final Function<C, T8> getter8,
            final StreamCodec<? super B, T9> codec9, final Function<C, T9> getter9,
            final StreamCodec<? super B, TA> codecA, final Function<C, TA> getterA,
            final Function10<T1,T2,T3,T4,T5,T6,T7,T8,T9,TA,C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                TA tA = codecA.decode(buffer);
                return factory.apply(t1,t2,t3,t4,t5,t6,t7,t8,t9,tA);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
                codec7.encode(buffer, getter7.apply(value));
                codec8.encode(buffer, getter8.apply(value));
                codec9.encode(buffer, getter9.apply(value));
                codecA.encode(buffer, getterA.apply(value));
            }
        };
    }

    public static <B,C,T1,T2,T3,T4,T5,T6,T7,T8,T9,TA,TB> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7,
            final StreamCodec<? super B, T8> codec8, final Function<C, T8> getter8,
            final StreamCodec<? super B, T9> codec9, final Function<C, T9> getter9,
            final StreamCodec<? super B, TA> codecA, final Function<C, TA> getterA,
            final StreamCodec<? super B, TB> codecB, final Function<C, TB> getterB,
            final Function11<T1,T2,T3,T4,T5,T6,T7,T8,T9,TA,TB,C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                TA tA = codecA.decode(buffer);
                TB tB = codecB.decode(buffer);
                return factory.apply(t1,t2,t3,t4,t5,t6,t7,t8,t9,tA,tB);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
                codec7.encode(buffer, getter7.apply(value));
                codec8.encode(buffer, getter8.apply(value));
                codec9.encode(buffer, getter9.apply(value));
                codecA.encode(buffer, getterA.apply(value));
                codecB.encode(buffer, getterB.apply(value));
            }
        };
    }

    public static <B,C,T1,T2,T3,T4,T5,T6,T7,T8,T9,TA,TB,TC> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7,
            final StreamCodec<? super B, T8> codec8, final Function<C, T8> getter8,
            final StreamCodec<? super B, T9> codec9, final Function<C, T9> getter9,
            final StreamCodec<? super B, TA> codecA, final Function<C, TA> getterA,
            final StreamCodec<? super B, TB> codecB, final Function<C, TB> getterB,
            final StreamCodec<? super B, TC> codecC, final Function<C, TC> getterC,
            final Function12<T1,T2,T3,T4,T5,T6,T7,T8,T9,TA,TB,TC,C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                TA tA = codecA.decode(buffer);
                TB tB = codecB.decode(buffer);
                TC tC = codecC.decode(buffer);
                return factory.apply(t1,t2,t3,t4,t5,t6,t7,t8,t9,tA,tB,tC);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
                codec7.encode(buffer, getter7.apply(value));
                codec8.encode(buffer, getter8.apply(value));
                codec9.encode(buffer, getter9.apply(value));
                codecA.encode(buffer, getterA.apply(value));
                codecB.encode(buffer, getterB.apply(value));
                codecC.encode(buffer, getterC.apply(value));
            }
        };
    }

    ///Combining SPart1 with normal "composite" stream codecs for up to 11 additional arguments (for a total of 12)
    public static <B,C,P1,T1> StreamCodec<B,C> combine(SPart1<? super B,C,P1> partial,
                                                       StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                       BiFunction<T1,P1,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                partial.codec1(),partial.getter1(),factory);
    }
    public static <B,C,P1,T1,T2> StreamCodec<B,C> combine(SPart1<? super B,C,P1> partial,
                                                          StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                          StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                          Function3<T1,T2,P1,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                codec2,getter2,
                partial.codec1(),partial.getter1(),factory);
    }

    public static <B,C,P1,T1,T2,T3> StreamCodec<B,C> combine(SPart1<? super B,C,P1> partial,
                                                             StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                             StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                             StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                             Function4<T1,T2,T3,P1,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                partial.codec1(),partial.getter1(),factory);
    }

    public static <B,C,P1,T1,T2,T3,T4> StreamCodec<B,C> combine(SPart1<? super B,C,P1> partial,
                                                                StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                Function5<T1,T2,T3,T4,P1,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                partial.codec1(),partial.getter1(),factory);
    }

    public static <B,C,P1,T1,T2,T3,T4,T5> StreamCodec<B,C> combine(SPart1<? super B,C,P1> partial,
                                                                   StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                   StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                   StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                   StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                   StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                   Function6<T1,T2,T3,T4,T5,P1,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                partial.codec1(),partial.getter1(),factory);
    }

    public static <B,C,P1,T1,T2,T3,T4,T5,T6> StreamCodec<B,C> combine(SPart1<? super B,C,P1> partial,
                                                                      StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                      StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                      StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                      StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                      StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                      StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                      Function7<T1,T2,T3,T4,T5,T6,P1,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                partial.codec1(),partial.getter1(),factory);
    }

    public static <B,C,P1,T1,T2,T3,T4,T5,T6,T7> StreamCodec<B,C> combine(SPart1<? super B,C,P1> partial,
                                                                         StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                         StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                         StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                         StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                         StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                         StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                         StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                         Function8<T1,T2,T3,T4,T5,T6,T7,P1,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                partial.codec1(),partial.getter1(),factory);
    }

    public static <B,C,P1,T1,T2,T3,T4,T5,T6,T7,T8> StreamCodec<B,C> combine(SPart1<? super B,C,P1> partial,
                                                                            StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                            StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                            StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                            StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                            StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                            StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                            StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                            StreamCodec<? super B,T8> codec8, Function<C,T8> getter8,
                                                                            Function9<T1,T2,T3,T4,T5,T6,T7,T8,P1,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                codec8,getter8,
                partial.codec1(),partial.getter1(),factory);
    }

    public static <B,C,P1,T1,T2,T3,T4,T5,T6,T7,T8,T9> StreamCodec<B,C> combine(SPart1<? super B,C,P1> partial,
                                                                               StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                               StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                               StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                               StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                               StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                               StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                               StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                               StreamCodec<? super B,T8> codec8, Function<C,T8> getter8,
                                                                               StreamCodec<? super B,T9> codec9, Function<C,T9> getter9,
                                                                               Function10<T1,T2,T3,T4,T5,T6,T7,T8,T9,P1,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                codec8,getter8,
                codec9,getter9,
                partial.codec1(),partial.getter1(),factory);
    }

    public static <B,C,P1,T1,T2,T3,T4,T5,T6,T7,T8,T9,TA> StreamCodec<B,C> combine(SPart1<? super B,C,P1> partial,
                                                                                  StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                                  StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                                  StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                                  StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                                  StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                                  StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                                  StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                                  StreamCodec<? super B,T8> codec8, Function<C,T8> getter8,
                                                                                  StreamCodec<? super B,T9> codec9, Function<C,T9> getter9,
                                                                                  StreamCodec<? super B,TA> codecA, Function<C,TA> getterA,
                                                                                  Function11<T1,T2,T3,T4,T5,T6,T7,T8,T9,TA,P1,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                codec8,getter8,
                codec9,getter9,
                codecA,getterA,
                partial.codec1(),partial.getter1(),factory);
    }

    public static <B,C,P1,T1,T2,T3,T4,T5,T6,T7,T8,T9,TA,TB> StreamCodec<B,C> combine(SPart1<? super B,C,P1> partial,
                                                                                     StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                                     StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                                     StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                                     StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                                     StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                                     StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                                     StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                                     StreamCodec<? super B,T8> codec8, Function<C,T8> getter8,
                                                                                     StreamCodec<? super B,T9> codec9, Function<C,T9> getter9,
                                                                                     StreamCodec<? super B,TA> codecA, Function<C,TA> getterA,
                                                                                     StreamCodec<? super B,TB> codecB, Function<C,TB> getterB,
                                                                                     Function12<T1,T2,T3,T4,T5,T6,T7,T8,T9,TA,TB,P1,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                codec8,getter8,
                codec9,getter9,
                codecA,getterA,
                codecB,getterB,
                partial.codec1(),partial.getter1(),factory);
    }

    ///Combining SPart2 with normal "composite" stream codecs for up to 10 additional arguments (for a total of 12)
    public static <B,C,P1,P2,T1> StreamCodec<B,C> combine(SPart2<? super B,C,P1,P2> partial,
                                                          StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                          Function3<T1,P1,P2,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),factory);
    }

    public static <B,C,P1,P2,T1,T2> StreamCodec<B,C> combine(SPart2<? super B,C,P1,P2> partial,
                                                             StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                             StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                             Function4<T1,T2,P1,P2,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                codec2,getter2,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),factory);
    }

    public static <B,C,P1,P2,T1,T2,T3> StreamCodec<B,C> combine(SPart2<? super B,C,P1,P2> partial,
                                                                StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                Function5<T1,T2,T3,P1,P2,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),factory);
    }

    public static <B,C,P1,P2,T1,T2,T3,T4> StreamCodec<B,C> combine(SPart2<? super B,C,P1,P2> partial,
                                                                   StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                   StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                   StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                   StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                   Function6<T1,T2,T3,T4,P1,P2,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),factory);
    }

    public static <B,C,P1,P2,T1,T2,T3,T4,T5> StreamCodec<B,C> combine(SPart2<? super B,C,P1,P2> partial,
                                                                      StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                      StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                      StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                      StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                      StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                      Function7<T1,T2,T3,T4,T5,P1,P2,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),factory);
    }

    public static <B,C,P1,P2,T1,T2,T3,T4,T5,T6> StreamCodec<B,C> combine(SPart2<? super B,C,P1,P2> partial,
                                                                         StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                         StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                         StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                         StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                         StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                         StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                         Function8<T1,T2,T3,T4,T5,T6,P1,P2,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),factory);
    }

    public static <B,C,P1,P2,T1,T2,T3,T4,T5,T6,T7> StreamCodec<B,C> combine(SPart2<? super B,C,P1,P2> partial,
                                                                            StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                            StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                            StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                            StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                            StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                            StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                            StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                            Function9<T1,T2,T3,T4,T5,T6,T7,P1,P2,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),factory);
    }

    public static <B,C,P1,P2,T1,T2,T3,T4,T5,T6,T7,T8> StreamCodec<B,C> combine(SPart2<? super B,C,P1,P2> partial,
                                                                               StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                               StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                               StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                               StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                               StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                               StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                               StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                               StreamCodec<? super B,T8> codec8, Function<C,T8> getter8,
                                                                               Function10<T1,T2,T3,T4,T5,T6,T7,T8,P1,P2,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                codec8,getter8,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),factory);
    }

    public static <B,C,P1,P2,T1,T2,T3,T4,T5,T6,T7,T8,T9> StreamCodec<B,C> combine(SPart2<? super B,C,P1,P2> partial,
                                                                                  StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                                  StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                                  StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                                  StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                                  StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                                  StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                                  StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                                  StreamCodec<? super B,T8> codec8, Function<C,T8> getter8,
                                                                                  StreamCodec<? super B,T9> codec9, Function<C,T9> getter9,
                                                                                  Function11<T1,T2,T3,T4,T5,T6,T7,T8,T9,P1,P2,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                codec8,getter8,
                codec9,getter9,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),factory);
    }

    public static <B,C,P1,P2,T1,T2,T3,T4,T5,T6,T7,T8,T9,TA> StreamCodec<B,C> combine(SPart2<? super B,C,P1,P2> partial,
                                                                                     StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                                     StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                                     StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                                     StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                                     StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                                     StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                                     StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                                     StreamCodec<? super B,T8> codec8, Function<C,T8> getter8,
                                                                                     StreamCodec<? super B,T9> codec9, Function<C,T9> getter9,
                                                                                     StreamCodec<? super B,TA> codecA, Function<C,TA> getterA,
                                                                                     Function12<T1,T2,T3,T4,T5,T6,T7,T8,T9,TA,P1,P2,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                codec8,getter8,
                codec9,getter9,
                codecA,getterA,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),factory);
    }

    ///Combining SPart3 with normal "composite" stream codecs for up to 9 additional arguments (for a total of 12)
    public static <B,C,P1,P2,P3,T1> StreamCodec<B,C> combine(SPart3<? super B,C,P1,P2,P3> partial,
                                                             StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                             Function4<T1,P1,P2,P3,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),factory);
    }

    public static <B,C,P1,P2,P3,T1,T2> StreamCodec<B,C> combine(SPart3<? super B,C,P1,P2,P3> partial,
                                                                StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                Function5<T1,T2,P1,P2,P3,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                codec2,getter2,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),factory);
    }

    public static <B,C,P1,P2,P3,T1,T2,T3> StreamCodec<B,C> combine(SPart3<? super B,C,P1,P2,P3> partial,
                                                                   StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                   StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                   StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                   Function6<T1,T2,T3,P1,P2,P3,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),factory);
    }

    public static <B,C,P1,P2,P3,T1,T2,T3,T4> StreamCodec<B,C> combine(SPart3<? super B,C,P1,P2,P3> partial,
                                                                      StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                      StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                      StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                      StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                      Function7<T1,T2,T3,T4,P1,P2,P3,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),factory);
    }

    public static <B,C,P1,P2,P3,T1,T2,T3,T4,T5> StreamCodec<B,C> combine(SPart3<? super B,C,P1,P2,P3> partial,
                                                                         StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                         StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                         StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                         StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                         StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                         Function8<T1,T2,T3,T4,T5,P1,P2,P3,C> factory) {

        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),factory);
    }

    public static <B,C,P1,P2,P3,T1,T2,T3,T4,T5,T6> StreamCodec<B,C> combine(SPart3<? super B,C,P1,P2,P3> partial,
                                                                            StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                            StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                            StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                            StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                            StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                            StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                            Function9<T1,T2,T3,T4,T5,T6,P1,P2,P3,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),factory);
    }

    public static <B,C,P1,P2,P3,T1,T2,T3,T4,T5,T6,T7> StreamCodec<B,C> combine(SPart3<? super B,C,P1,P2,P3> partial,
                                                                               StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                               StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                               StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                               StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                               StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                               StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                               StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                               Function10<T1,T2,T3,T4,T5,T6,T7,P1,P2,P3,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),factory);
    }

    public static <B,C,P1,P2,P3,T1,T2,T3,T4,T5,T6,T7,T8> StreamCodec<B,C> combine(SPart3<? super B,C,P1,P2,P3> partial,
                                                                                  StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                                  StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                                  StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                                  StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                                  StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                                  StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                                  StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                                  StreamCodec<? super B,T8> codec8, Function<C,T8> getter8,
                                                                                  Function11<T1,T2,T3,T4,T5,T6,T7,T8,P1,P2,P3,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                codec8,getter8,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),factory);
    }

    public static <B,C,P1,P2,P3,T1,T2,T3,T4,T5,T6,T7,T8,T9> StreamCodec<B,C> combine(SPart3<? super B,C,P1,P2,P3> partial,
                                                                                     StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                                     StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                                     StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                                     StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                                     StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                                     StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                                     StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                                     StreamCodec<? super B,T8> codec8, Function<C,T8> getter8,
                                                                                     StreamCodec<? super B,T9> codec9, Function<C,T9> getter9,
                                                                                     Function12<T1,T2,T3,T4,T5,T6,T7,T8,T9,P1,P2,P3,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                codec8,getter8,
                codec9,getter9,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),factory);
    }

    ///Combining SPart4 with normal "composite" stream codecs for up to 8 additional arguments (for a total of 12)
    public static <B,C,P1,P2,P3,P4,T1> StreamCodec<B,C> combine(SPart4<? super B,C,P1,P2,P3,P4> partial,
                                                                StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                Function5<T1,P1,P2,P3,P4,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),factory);
    }

    public static <B,C,P1,P2,P3,P4,T1,T2> StreamCodec<B,C> combine(SPart4<? super B,C,P1,P2,P3,P4> partial,
                                                                   StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                   StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                   Function6<T1,T2,P1,P2,P3,P4,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                codec2,getter2,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),factory);
    }

    public static <B,C,P1,P2,P3,P4,T1,T2,T3> StreamCodec<B,C> combine(SPart4<? super B,C,P1,P2,P3,P4> partial,
                                                                      StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                      StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                      StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                      Function7<T1,T2,T3,P1,P2,P3,P4,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),factory);
    }

    public static <B,C,P1,P2,P3,P4,T1,T2,T3,T4> StreamCodec<B,C> combine(SPart4<? super B,C,P1,P2,P3,P4> partial,
                                                                         StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                         StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                         StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                         StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                         Function8<T1,T2,T3,T4,P1,P2,P3,P4,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),factory);
    }

    public static <B,C,P1,P2,P3,P4,T1,T2,T3,T4,T5> StreamCodec<B,C> combine(SPart4<? super B,C,P1,P2,P3,P4> partial,
                                                                            StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                            StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                            StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                            StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                            StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                            Function9<T1,T2,T3,T4,T5,P1,P2,P3,P4,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),factory);
    }

    public static <B,C,P1,P2,P3,P4,T1,T2,T3,T4,T5,T6> StreamCodec<B,C> combine(SPart4<? super B,C,P1,P2,P3,P4> partial,
                                                                               StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                               StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                               StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                               StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                               StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                               StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                               Function10<T1,T2,T3,T4,T5,T6,P1,P2,P3,P4,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),factory);
    }

    public static <B,C,P1,P2,P3,P4,T1,T2,T3,T4,T5,T6,T7> StreamCodec<B,C> combine(SPart4<? super B,C,P1,P2,P3,P4> partial,
                                                                                  StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                                  StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                                  StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                                  StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                                  StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                                  StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                                  StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                                  Function11<T1,T2,T3,T4,T5,T6,T7,P1,P2,P3,P4,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),factory);
    }

    public static <B,C,P1,P2,P3,P4,T1,T2,T3,T4,T5,T6,T7,T8> StreamCodec<B,C> combine(SPart4<? super B,C,P1,P2,P3,P4> partial,
                                                                                     StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                                     StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                                     StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                                     StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                                     StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                                     StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                                     StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                                     StreamCodec<? super B,T8> codec8, Function<C,T8> getter8,
                                                                                     Function12<T1,T2,T3,T4,T5,T6,T7,T8,P1,P2,P3,P4,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                codec8,getter8,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),factory);
    }

    ///Combining SPart5 with normal "composite" stream codecs for up to 7 additional arguments (for a total of 12)
    public static <B,C,P1,P2,P3,P4,P5,T1> StreamCodec<B,C> combine(SPart5<? super B,C,P1,P2,P3,P4,P5> partial,
                                                                   StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                   Function6<T1,P1,P2,P3,P4,P5,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),partial.codec5(),partial.getter5(),factory);
    }

    public static <B,C,P1,P2,P3,P4,P5,T1,T2> StreamCodec<B,C> combine(SPart5<? super B,C,P1,P2,P3,P4,P5> partial,
                                                                      StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                      StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                      Function7<T1,T2,P1,P2,P3,P4,P5,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),partial.codec5(),partial.getter5(),factory);
    }

    public static <B,C,P1,P2,P3,P4,P5,T1,T2,T3> StreamCodec<B,C> combine(SPart5<? super B,C,P1,P2,P3,P4,P5> partial,
                                                                         StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                         StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                         StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                         Function8<T1,T2,T3,P1,P2,P3,P4,P5,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),partial.codec5(),partial.getter5(),factory);
    }

    public static <B,C,P1,P2,P3,P4,P5,T1,T2,T3,T4> StreamCodec<B,C> combine(SPart5<? super B,C,P1,P2,P3,P4,P5> partial,
                                                                            StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                            StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                            StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                            StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                            Function9<T1,T2,T3,T4,P1,P2,P3,P4,P5,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),partial.codec5(),partial.getter5(),factory);
    }

    public static <B,C,P1,P2,P3,P4,P5,T1,T2,T3,T4,T5> StreamCodec<B,C> combine(SPart5<? super B,C,P1,P2,P3,P4,P5> partial,
                                                                               StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                               StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                               StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                               StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                               StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                               Function10<T1,T2,T3,T4,T5,P1,P2,P3,P4,P5,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),partial.codec5(),partial.getter5(),factory);
    }

    public static <B,C,P1,P2,P3,P4,P5,T1,T2,T3,T4,T5,T6> StreamCodec<B,C> combine(SPart5<? super B,C,P1,P2,P3,P4,P5> partial,
                                                                                  StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                                  StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                                  StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                                  StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                                  StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                                  StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                                  Function11<T1,T2,T3,T4,T5,T6,P1,P2,P3,P4,P5,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),partial.codec5(),partial.getter5(),factory);
    }

    public static <B,C,P1,P2,P3,P4,P5,T1,T2,T3,T4,T5,T6,T7> StreamCodec<B,C> combine(SPart5<? super B,C,P1,P2,P3,P4,P5> partial,
                                                                                     StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                                     StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                                     StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                                     StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                                     StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                                     StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                                     StreamCodec<? super B,T7> codec7, Function<C,T7> getter7,
                                                                                     Function12<T1,T2,T3,T4,T5,T6,T7,P1,P2,P3,P4,P5,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                codec7,getter7,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),partial.codec5(),partial.getter5(),factory);
    }

    ///Combining SPart6 with normal "composite" stream codecs for up to 6 additional arguments (for a total of 12)
    public static <B,C,P1,P2,P3,P4,P5,P6,T1> StreamCodec<B,C> combine(SPart6<? super B,C,P1,P2,P3,P4,P5,P6> partial,
                                                                      StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                      Function7<T1,P1,P2,P3,P4,P5,P6,C> factory) {
        return composite(
                codec1,getter1,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),partial.codec5(),partial.getter5(),partial.codec6(),partial.getter6(),factory);
    }

    public static <B,C,P1,P2,P3,P4,P5,P6,T1,T2> StreamCodec<B,C> combine(SPart6<? super B,C,P1,P2,P3,P4,P5,P6> partial,
                                                                         StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                         StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                         Function8<T1,T2,P1,P2,P3,P4,P5,P6,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),partial.codec5(),partial.getter5(),partial.codec6(),partial.getter6(),factory);
    }

    public static <B,C,P1,P2,P3,P4,P5,P6,T1,T2,T3> StreamCodec<B,C> combine(SPart6<? super B,C,P1,P2,P3,P4,P5,P6> partial,
                                                                            StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                            StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                            StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                            Function9<T1,T2,T3,P1,P2,P3,P4,P5,P6,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),partial.codec5(),partial.getter5(),partial.codec6(),partial.getter6(),factory);
    }

    public static <B,C,P1,P2,P3,P4,P5,P6,T1,T2,T3,T4> StreamCodec<B,C> combine(SPart6<? super B,C,P1,P2,P3,P4,P5,P6> partial,
                                                                               StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                               StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                               StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                               StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                               Function10<T1,T2,T3,T4,P1,P2,P3,P4,P5,P6,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),partial.codec5(),partial.getter5(),partial.codec6(),partial.getter6(),factory);
    }

    public static <B,C,P1,P2,P3,P4,P5,P6,T1,T2,T3,T4,T5> StreamCodec<B,C> combine(SPart6<? super B,C,P1,P2,P3,P4,P5,P6> partial,
                                                                                  StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                                  StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                                  StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                                  StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                                  StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                                  Function11<T1,T2,T3,T4,T5,P1,P2,P3,P4,P5,P6,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),partial.codec5(),partial.getter5(),partial.codec6(),partial.getter6(),factory);
    }

    public static <B,C,P1,P2,P3,P4,P5,P6,T1,T2,T3,T4,T5,T6> StreamCodec<B,C> combine(SPart6<? super B,C,P1,P2,P3,P4,P5,P6> partial,
                                                                                     StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
                                                                                     StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
                                                                                     StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
                                                                                     StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
                                                                                     StreamCodec<? super B,T5> codec5, Function<C,T5> getter5,
                                                                                     StreamCodec<? super B,T6> codec6, Function<C,T6> getter6,
                                                                                     Function12<T1,T2,T3,T4,T5,T6,P1,P2,P3,P4,P5,P6,C> factory) {
        return composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                codec6,getter6,
                partial.codec1(),partial.getter1(),partial.codec2(),partial.getter2(),partial.codec3(),partial.getter3(),partial.codec4(),partial.getter4(),partial.codec5(),partial.getter5(),partial.codec6(),partial.getter6(),factory);
    }


}