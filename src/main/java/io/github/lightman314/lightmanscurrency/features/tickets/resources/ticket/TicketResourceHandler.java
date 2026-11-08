package io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket;

import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub.TicketStubCollector;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public interface TicketResourceHandler extends TicketStubCollector {

    boolean hasUnlimitedUses(long ticketID);
    long getCount(long ticketID);
    default int collectTicket(long ticketID,int count,TransactionContext transaction) { return this.collectTicket(ticketID,count,this,transaction); }
    int collectTicket(long ticketID,int count,TicketStubCollector stubCollector,TransactionContext transaction);

}