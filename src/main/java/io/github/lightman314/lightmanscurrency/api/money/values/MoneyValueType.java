package io.github.lightman314.lightmanscurrency.api.money.values;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.money.values.parsing.MoneyValueParser;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record MoneyValueType<T extends MoneyValue>(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec,MoneyValueParser<T> parser) {

}