package io.github.lightman314.lightmanscurrency.api.ejection;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class EjectionDataType<T extends EjectionData> {

    public abstract MapCodec<T> mapCodec();
    public abstract StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec();

    public EjectionData loadOldData(CompoundTag tag, HolderLookup.Provider lookup, long id) { return null; }

}
