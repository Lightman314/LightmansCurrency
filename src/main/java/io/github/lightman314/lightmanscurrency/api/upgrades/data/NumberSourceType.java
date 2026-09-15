package io.github.lightman314.lightmanscurrency.api.upgrades.data;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class NumberSourceType<T extends NumberSource> extends AbstractType.Serializable<T,NumberSourceType<?>> {

    public NumberSourceType(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) { super(codec, streamCodec); }

    @Override
    protected Registry<NumberSourceType<?>> getRegistry() { return LCRegistries.Upgrades.NUMBER_SOURCE; }
    @Override
    protected String getName() { return "NumberSourceType"; }

}