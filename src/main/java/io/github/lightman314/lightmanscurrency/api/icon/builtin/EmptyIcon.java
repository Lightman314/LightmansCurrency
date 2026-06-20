package io.github.lightman314.lightmanscurrency.api.icon.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.IconType;
import net.minecraft.network.codec.StreamCodec;

public final class EmptyIcon extends IconData {

    public static final EmptyIcon INSTANCE = new EmptyIcon();
    public static final IconType<EmptyIcon> TYPE = new IconType<>(MapCodec.unit(INSTANCE),StreamCodec.unit(INSTANCE));

    private EmptyIcon() {}

    @Override
    public IconType<?> getType() { return TYPE; }

}