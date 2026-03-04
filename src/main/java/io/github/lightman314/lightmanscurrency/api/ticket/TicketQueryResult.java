package io.github.lightman314.lightmanscurrency.api.ticket;

public enum TicketQueryResult {

    PRESENT,UNLIMITED,FAIL;

    public boolean isPresent() { return this == PRESENT || this == UNLIMITED; }
    public boolean failed() { return this == FAIL; }
    public boolean isUnlimited() { return this == UNLIMITED; }

}
