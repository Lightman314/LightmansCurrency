package io.github.lightman314.lightmanscurrency.api.helpers.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Record used to store a stream codec used to encode/decode this entry from a FancyPacketMap/List<br>
 * Registry doesn't take the Stream Codec directly so that this record can be used to confirm that the stream codec is registered (or at least pretending to be registered)
 * @param codec
 * @param <T>
 */
public record FancyPacketType<T>(StreamCodec<? super RegistryFriendlyByteBuf,T> codec) {

    public FancyPacketMap.Entry<T> buildEntry(T value) { return new FancyPacketMap.Entry<>(this,value); }

}