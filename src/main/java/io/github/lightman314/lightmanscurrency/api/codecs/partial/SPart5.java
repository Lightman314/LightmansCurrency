package io.github.lightman314.lightmanscurrency.api.codecs.partial;

import com.mojang.datafixers.util.Function5;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

public record SPart5<B,C,T1,T2,T3,T4,T5>(
        StreamCodec<? super B,T1> codec1, Function<C,T1> getter1,
        StreamCodec<? super B,T2> codec2, Function<C,T2> getter2,
        StreamCodec<? super B,T3> codec3, Function<C,T3> getter3,
        StreamCodec<? super B,T4> codec4, Function<C,T4> getter4,
        StreamCodec<? super B,T5> codec5, Function<C,T5> getter5
) {

    public static <B,C,T1,T2,T3,T4,T5> SPart5<B,C,T1,T2,T3,T4,T5> of(SPart1<? super B,C,T5> part1
            ,StreamCodec<? super B,T1> codec1,Function<C,T1> getter1
            ,StreamCodec<? super B,T2> codec2,Function<C,T2> getter2
            ,StreamCodec<? super B,T3> codec3,Function<C,T3> getter3
            ,StreamCodec<? super B,T4> codec4,Function<C,T4> getter4
    ) { return new SPart5<>(codec1,getter1,codec2,getter2,codec3,getter3,codec4,getter4,part1.codec1(),part1.getter1()); }
    public static <B,C,T1,T2,T3,T4,T5> SPart5<B,C,T1,T2,T3,T4,T5> of(SPart2<? super B,C,T4,T5> part2
            ,StreamCodec<? super B,T1> codec1,Function<C,T1> getter1
            ,StreamCodec<? super B,T2> codec2,Function<C,T2> getter2
            ,StreamCodec<? super B,T3> codec3,Function<C,T3> getter3
    ) { return new SPart5<>(codec1,getter1,codec2,getter2,codec3,getter3,part2.codec1(), part2.getter1(),part2.codec2(), part2.getter2()); }
    public static <B,C,T1,T2,T3,T4,T5> SPart5<B,C,T1,T2,T3,T4,T5> of(SPart3<? super B,C,T3,T4,T5> part3
            ,StreamCodec<? super B,T1> codec1,Function<C,T1> getter1
            ,StreamCodec<? super B,T2> codec2,Function<C,T2> getter2
    ) { return new SPart5<>(codec1,getter1,codec2,getter2,part3.codec1(),part3.getter1(),part3.codec2(),part3.getter2(),part3.codec3(),part3.getter3()); }
    public static <B,C,T1,T2,T3,T4,T5> SPart5<B,C,T1,T2,T3,T4,T5> of(SPart4<? super B,C,T2,T3,T4,T5> part4
            ,StreamCodec<? super B,T1> codec1,Function<C,T1> getter1
    ) { return new SPart5<>(codec1,getter1,part4.codec1(),part4.getter1(),part4.codec2(),part4.getter2(),part4.codec3(),part4.getter3(),part4.codec4(),part4.getter4()); }

    public <T extends C> StreamCodec<B,C> assemble(Function5<T1,T2,T3,T4,T5,C> factory) {
        return StreamCodec.composite(
                codec1,getter1,
                codec2,getter2,
                codec3,getter3,
                codec4,getter4,
                codec5,getter5,
                factory);
    }

}
