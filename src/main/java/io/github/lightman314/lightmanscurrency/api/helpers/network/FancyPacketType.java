package io.github.lightman314.lightmanscurrency.api.helpers.network;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Record used to store a stream codec used to encode/decode this entry from a FancyPacketMap/List<br>
 * Registry doesn't take the Stream Codec directly so that this record can be used to confirm that the stream codec is registered (or at least pretending to be registered)
 */
public final class FancyPacketType<T> extends AbstractType<FancyPacketType<?>> {

    private final StreamCodec<? super  RegistryFriendlyByteBuf,T> codec;
    public StreamCodec<? super RegistryFriendlyByteBuf,T> codec() { return this.codec; }
    public FancyPacketType(StreamCodec<? super RegistryFriendlyByteBuf,T> codec) { this.codec = codec; }

    public FancyPacketMap.Entry<T> buildEntry(T value) { return new FancyPacketMap.Entry<>(this,value); }

    @Override
    protected Registry<FancyPacketType<?>> getRegistry() { return LCRegistries.Network.PACKET_TYPE; }

    @Override
    protected String getName() { return "FancyPacketType"; }
}