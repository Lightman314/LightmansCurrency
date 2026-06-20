package io.github.lightman314.lightmanscurrency.api.codecs.partial;

import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

public record SPart1<B,C,T1>(
        StreamCodec<? super B,T1> codec1, Function<C,T1> getter1
) {

    public StreamCodec<B,C> assemble(Function<T1,C> factory) { return StreamCodec.composite(codec1,getter1,factory); }

}