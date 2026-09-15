package io.github.lightman314.lightmanscurrency.api.ownership;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class OwnerType<T extends Owner> extends AbstractType.Serializable<T,OwnerType<?>> {

    public OwnerType(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) { super(codec, streamCodec); }

    @Override
    protected Registry<OwnerType<?>> getRegistry() { return LCRegistries.Ownership.OWNER_TYPE; }
    @Override
    protected String getName() { return "OwnerType"; }

}