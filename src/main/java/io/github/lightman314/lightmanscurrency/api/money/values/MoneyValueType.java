package io.github.lightman314.lightmanscurrency.api.money.values;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import io.github.lightman314.lightmanscurrency.api.money.values.parsing.MoneyValueParser;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class MoneyValueType<T extends MoneyValue> extends AbstractType.Serializable<T,MoneyValueType<?>> {

    private final MoneyValueParser<T> parser;
    public MoneyValueParser<T> parser() { return this.parser; }

    public MoneyValueType(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec,MoneyValueParser<T> parser) {
        super(codec, streamCodec);
        this.parser = parser;
    }

    @Override
    protected Registry<MoneyValueType<?>> getRegistry() { return LCRegistries.Money.VALUE_TYPE; }
    @Override
    protected String getName() { return "MoneyValueType"; }

}