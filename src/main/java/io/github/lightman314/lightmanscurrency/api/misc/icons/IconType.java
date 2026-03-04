package io.github.lightman314.lightmanscurrency.api.misc.icons;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class IconType<T extends IconData> {

    public abstract T loadOld(CompoundTag tag, HolderLookup.Provider lookup);
    public abstract T parseOld(JsonObject json, HolderLookup.Provider lookup);

    public abstract MapCodec<T> codec();
    public abstract StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec();

    @Override
    public int hashCode() { return LCRegistries.ICON_TYPE.getKey(this).hashCode(); }
    @Override
    public String toString() { return "IconType[" + LCRegistries.ICON_TYPE.getKey(this) + "]"; }

}
