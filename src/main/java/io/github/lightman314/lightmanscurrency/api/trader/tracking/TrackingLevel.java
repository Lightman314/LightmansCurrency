package io.github.lightman314.lightmanscurrency.api.trader.tracking;

import com.google.common.collect.ImmutableList;

public enum TrackingLevel {
    NONE,CUSTOMER,STORAGE;
    public boolean isLevel(TrackingLevel level) { return this.ordinal() >= level.ordinal(); }
    public static final Iterable<TrackingLevel> HIGHEST_TO_LOWEST = ImmutableList.of(STORAGE,CUSTOMER);
}