package io.github.lightman314.lightmanscurrency.api.ownership;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class OwnerType<T extends Owner> {

    public abstract MapCodec<T> codec();
    public abstract StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec();

}