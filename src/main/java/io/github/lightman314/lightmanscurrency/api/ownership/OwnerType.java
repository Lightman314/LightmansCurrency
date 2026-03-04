package io.github.lightman314.lightmanscurrency.api.ownership;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public abstract class OwnerType<T extends Owner> {

    public static final Codec<OwnerType<?>> CODEC = LCRegistries.OWNER_TYPES.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf,OwnerType<?>> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.OWNER_TYPE_KEY);

    public abstract Owner loadOldData(CompoundTag tag, HolderLookup.Provider lookup);
    public abstract MapCodec<T> codec();
    public abstract StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec();

}
