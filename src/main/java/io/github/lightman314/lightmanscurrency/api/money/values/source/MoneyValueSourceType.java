package io.github.lightman314.lightmanscurrency.api.money.values.source;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class MoneyValueSourceType<T extends MoneyValueSource> extends AbstractType.Serializable<T,MoneyValueSourceType<?>> {

    public MoneyValueSourceType(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) { super(codec, streamCodec); }

    @Override
    protected Registry<MoneyValueSourceType<?>> getRegistry() { return LCRegistries.Money.VALUE_SOURCE; }
    @Override
    protected String getName() { return "MoneyValueSourceType"; }

}
