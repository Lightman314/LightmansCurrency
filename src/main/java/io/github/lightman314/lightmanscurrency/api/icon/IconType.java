package io.github.lightman314.lightmanscurrency.api.icon;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class IconType<T extends IconData> extends AbstractType.Serializable<T,IconType<?>> {

    public IconType(MapCodec<T> codec,StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) { super(codec,streamCodec); }

    @Override
    protected Registry<IconType<?>> getRegistry() { return LCRegistries.Misc.ICON_TYPE; }

    @Override
    protected String getName() { return "IconType"; }

}