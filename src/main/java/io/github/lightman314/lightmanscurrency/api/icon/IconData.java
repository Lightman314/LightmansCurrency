package io.github.lightman314.lightmanscurrency.api.icon;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.EmptyIcon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public abstract class IconData {

    public static final Codec<IconData> CODEC = LCRegistries.Misc.ICON_TYPE.byNameCodec()
            .dispatch(IconData::getType,IconType::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf,IconData> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Misc.ICON_TYPE_KEY)
            .dispatch(IconData::getType,IconType::streamCodec);

    public static IconData empty() { return EmptyIcon.INSTANCE; }

    public abstract IconType<?> getType();

}