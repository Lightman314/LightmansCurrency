package io.github.lightman314.lightmanscurrency.features.trader.paygate;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.text.TextEntryBundle;
import net.minecraft.network.chat.Component;

public enum OutputConflictHandling {
    DENY_ANY(false),
    DENY_SIDE_CONFLICT(false),
    ADD_TIME(true),
    OVERRIDE_TIME(true);

    public static final Codec<OutputConflictHandling> CODEC = EnumHelper.buildCodec(OutputConflictHandling.class,"OutputConflictHandling");
    public static final TextEntryBundle<OutputConflictHandling> TEXT = TextEntryBundle.of(values(),"gui.trader.paygate.conflict");

    public final boolean allowsConflicts;
    public Component getName() { return TEXT.getComponent(this); }
    OutputConflictHandling(boolean allowsConflicts) { this.allowsConflicts = allowsConflicts; }

}