package io.github.lightman314.lightmanscurrency.api.ticket;

public enum TicketCollectionResult {

    PASS(true,false),PASS_WITH_STUB(true,true),FAIL(false,false);
    public final boolean passed;
    public final boolean failed;
    public final boolean spawnTicketStub;
    TicketCollectionResult(boolean passed,boolean spawnTicketStub) { this.passed = passed; this.failed = !this.passed; this.spawnTicketStub = spawnTicketStub; }

    public static TicketCollectionResult pass(boolean spawnTicketStub) { return spawnTicketStub ? PASS_WITH_STUB : PASS; }

}