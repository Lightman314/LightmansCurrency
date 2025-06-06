package io.github.lightman314.lightmanscurrency.common.traders.paygate;

public enum OutputConflictHandling {
    DENY_ANY(false),
    DENY_SIDE_CONFLICT(false),
    ADD_TIME(true),
    OVERRIDE_TIME(true);

    public final boolean allowsConflicts;
    OutputConflictHandling(boolean allowsConflicts) { this.allowsConflicts = allowsConflicts; }
}
