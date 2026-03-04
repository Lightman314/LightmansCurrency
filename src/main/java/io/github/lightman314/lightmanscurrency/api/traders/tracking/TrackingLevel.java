package io.github.lightman314.lightmanscurrency.api.traders.tracking;

public enum TrackingLevel {
    NONE,CUSTOMER,STORAGE;
    public boolean isLevel(TrackingLevel level) { return this.ordinal() >= level.ordinal(); }
}