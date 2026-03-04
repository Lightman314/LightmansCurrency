package io.github.lightman314.lightmanscurrency.api.codecs.partial;

import com.mojang.datafixers.util.Function4;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

public record SPart4<B,C,T1,T2,T3,T4>(
        StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
        StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
        StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
        StreamCodec<? super B,T4> codec4, Function<C,T4> getter4
) {

    public static <B,C,T1,T2,T3,T4> SPart4<B,C,T1,T2,T3,T4> of(SPart1<? super B,C,T4> part1
            ,StreamCodec<? super B,T1> codec1,Function<C,T1> getter1
            ,StreamCodec<? super B,T2> codec2,Function<C,T2> getter2
            ,StreamCodec<? super B,T3> codec3,Function<C,T3> getter3
    ) { return new SPart4<>(codec1,getter1,codec2,getter2,codec3,getter3,part1.codec1(),part1.getter1()); }
    public static <B,C,T1,T2,T3,T4> SPart4<B,C,T1,T2,T3,T4> of(SPart2<? super B,C,T3,T4> part2
            ,StreamCodec<? super B,T1> codec1,Function<C,T1> getter1
            ,StreamCodec<? super B,T2> codec2,Function<C,T2> getter2
    ) { return new SPart4<>(codec1,getter1,codec2,getter2,part2.codec1(), part2.getter1(),part2.codec2(), part2.getter2()); }
    public static <B,C,T1,T2,T3,T4> SPart4<B,C,T1,T2,T3,T4> of(SPart3<? super B,C,T2,T3,T4> part3
            ,StreamCodec<? super B,T1> codec1,Function<C,T1> getter1
    ) { return new SPart4<>(codec1,getter1,part3.codec1(),part3.getter1(),part3.codec2(),part3.getter2(),part3.codec3(),part3.getter3()); }

    public <T extends C> StreamCodec<B,C> assemble(Function4<T1,T2,T3,T4,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                factory);
    }

}
