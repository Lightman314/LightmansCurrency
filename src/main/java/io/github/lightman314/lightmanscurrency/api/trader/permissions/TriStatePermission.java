package io.github.lightman314.lightmanscurrency.api.trader.permissions;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum TriStatePermission implements StringRepresentable {
    NONE, LOW, HIGH;

    public boolean hasLowerPermission() { return this == LOW || this == HIGH; }
    public boolean hasHigherPermission() { return this == HIGH; }

    public static final Codec<TriStatePermission> CODEC = StringRepresentable.fromEnum(TriStatePermission::values);
    public static final StreamCodec<ByteBuf, TriStatePermission> STREAM_CODEC = EnumHelper.buildStreamCodec(TriStatePermission.class,"TriState");
    @Override
    public String getSerializedName() { return this.name().toLowerCase(Locale.ROOT); }
}
