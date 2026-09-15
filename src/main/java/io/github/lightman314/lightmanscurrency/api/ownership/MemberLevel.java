package io.github.lightman314.lightmanscurrency.api.ownership;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.text.TextEntryBundle;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;

public enum MemberLevel {
    MEMBERS,ADMINS,OWNER;

    public static final Codec<MemberLevel> CODEC = EnumHelper.buildCodec(MemberLevel.class,"Member Level");
    public static final StreamCodec<ByteBuf,MemberLevel> STREAM_CODEC = EnumHelper.buildStreamCodec(MemberLevel.class,"Member Level");

    public static final TextEntryBundle<MemberLevel> BLURB = TextEntryBundle.of(MemberLevel.values(),"blurb.lightmanscurrency.ownership");

    public MemberLevel next() { return EnumHelper.enumFromOrdinal(this.ordinal() + 1,values(),MEMBERS); }

    public final MutableComponent getBlurb() { return BLURB.getComponent(this); }

}
