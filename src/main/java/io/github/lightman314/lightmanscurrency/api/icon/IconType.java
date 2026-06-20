package io.github.lightman314.lightmanscurrency.api.icon;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.RegistryHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class IconType<T extends IconData> {

    private final MapCodec<T> codec;
    public MapCodec<T> codec() { return this.codec; }
    private final StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec;
    public StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec() { return this.streamCodec; }
    public IconType(MapCodec<T> codec,StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) { this.codec = codec; this.streamCodec = streamCodec; }

    @Override
    public int hashCode() { return RegistryHelper.hash(LCRegistries.Misc.ICON_TYPE,this); }
    @Override
    public String toString() { return RegistryHelper.toString("IconType",LCRegistries.Misc.ICON_TYPE,this); }
}