package io.github.lightman314.lightmanscurrency.api.upgrades.data;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record NumberSourceType<T extends NumberSource>(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) {  }