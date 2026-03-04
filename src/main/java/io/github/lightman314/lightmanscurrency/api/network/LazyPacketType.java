package io.github.lightman314.lightmanscurrency.api.network;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * A holder for StreamCodecs, to be registered to {@link LCRegistries#LAZY_PACKETS LCRegistries#LAZY_PACKETS}.<br>
 * Allows the use of {@link LazyPacketData.Builder#setCustom(String, Object, LazyPacketType)} and {@link LazyPacketData#getCustom(String, LazyPacketType)} for easier and more efficient read/write operations
 * @param codec
 * @param <T>
 */
public record LazyPacketType<T>(StreamCodec<? super RegistryFriendlyByteBuf,T> codec) { }