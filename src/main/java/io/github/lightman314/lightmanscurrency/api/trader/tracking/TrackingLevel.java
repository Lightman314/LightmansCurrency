package io.github.lightman314.lightmanscurrency.api.trader.tracking;

import com.google.common.collect.ImmutableList;

public enum TrackingLevel {
    NONE,CUSTOMER,STORAGE;
    private static final TrackingLevel[] NON_EMPTY_VALUES = new TrackingLevel[] {TrackingLevel.CUSTOMER,TrackingLevel.STORAGE };
    public static TrackingLevel[] nonEmptyValues() { return NON_EMPTY_VALUES; }
    public boolean isLevel(TrackingLevel level) { return this.ordinal() >= level.ordinal(); }
    public static final Iterable<TrackingLevel> HIGHEST_TO_LOWEST = ImmutableList.of(STORAGE,CUSTOMER);
}