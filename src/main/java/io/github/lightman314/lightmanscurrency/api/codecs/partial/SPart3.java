package io.github.lightman314.lightmanscurrency.api.codecs.partial;

import com.mojang.datafixers.util.Function3;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

public record SPart3<B,C,T1,T2,T3>(
        StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
        StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
        StreamCodec<? super B,T3> codec3, Function<C,T3> getter3
) {

    public static <B,C,T1,T2,T3> SPart3<B,C,T1,T2,T3> of(SPart1<? super B,C,T3> part1
            ,StreamCodec<? super B,T1> codec1,Function<C,T1> getter1
            ,StreamCodec<? super B,T2> codec2,Function<C,T2> getter2
    ) { return new SPart3<>(codec1,getter1,codec2,getter2,part1.codec1(),part1.getter1()); }
    public static <B,C,T1,T2,T3> SPart3<B,C,T1,T2,T3> of(SPart2<? super B,C,T2,T3> part2
            ,StreamCodec<? super B,T1> codec1,Function<C,T1> getter1
    ) { return new SPart3<>(codec1,getter1,part2.codec1(), part2.getter1(),part2.codec2(), part2.getter2()); }

    public StreamCodec<B,C> assemble(Function3<T1,T2,T3,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                factory);
    }

}
