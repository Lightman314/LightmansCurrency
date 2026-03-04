package io.github.lightman314.lightmanscurrency.api.ticket;

public enum TicketCollectionResult {

    PASS,PASS_WITH_STUB,FAIL;
    public boolean isSuccess() { return this == PASS || this == PASS_WITH_STUB; }
    public boolean failed() { return this == FAIL; }
    public boolean spawnTicketStub() { return this == PASS_WITH_STUB; }

    public static TicketCollectionResult pass(boolean spawnTicketStub) { return spawnTicketStub ? PASS_WITH_STUB : PASS; }

}
