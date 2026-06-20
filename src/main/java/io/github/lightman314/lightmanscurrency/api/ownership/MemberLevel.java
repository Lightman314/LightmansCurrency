package io.github.lightman314.lightmanscurrency.api.ownership;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import net.minecraft.network.chat.MutableComponent;

public enum MemberLevel {
    MEMBERS,ADMINS,OWNER;

    public static final Codec<MemberLevel> CODEC = EnumHelper.buildCodec(MemberLevel.class,"Member Level");

    public MemberLevel next() { return EnumHelper.enumFromOrdinal(this.ordinal() + 1,values(),OWNER); }

    public final MutableComponent getBlurb() { return LCText.Ownership.BLURB_OWNERSHIP.getComponent(this); }

}
