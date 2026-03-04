package io.github.lightman314.lightmanscurrency.api.codecs.partial;

import net.minecraft.network.codec.StreamCodec;

import java.util.function.BiFunction;
import java.util.function.Function;

public record SPart2<B,C,T1,T2>(
        StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
        StreamCodec<? super B,T2> codec2, Function<C,T2> getter2
) {

    public static <B,C,T1,T2> SPart2<B,C,T1,T2> of(SPart1<? super B,C,T2> part1
            ,StreamCodec<? super B,T1> codec1,Function<C,T1> getter1
    ) { return new SPart2<>(codec1,getter1,part1.codec1(),part1.getter1()); }

    public StreamCodec<B,C> assemble(BiFunction<T1,T2,C> factory) {
        return StreamCodec.composite(
            codec1,getter1,
            codec2,getter2,
            factory);
    }

}