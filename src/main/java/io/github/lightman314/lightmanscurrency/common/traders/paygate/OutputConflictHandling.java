package io.github.lightman314.lightmanscurrency.common.traders.paygate;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;

public enum OutputConflictHandling {
    DENY_ANY(false),
    DENY_SIDE_CONFLICT(false),
    ADD_TIME(true),
    OVERRIDE_TIME(true);

    public static final Codec<OutputConflictHandling> CODEC = EnumUtil.buildCodec(OutputConflictHandling.class,"Output Conflict Handling");

    public final boolean allowsConflicts;
    OutputConflictHandling(boolean allowsConflicts) { this.allowsConflicts = allowsConflicts; }
}
