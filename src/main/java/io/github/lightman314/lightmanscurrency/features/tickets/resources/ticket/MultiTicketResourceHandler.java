package io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket;

import io.github.lightman314.lightmanscurrency.features.tickets.resources.EmptyTicketlikeResourceHandler;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub.TicketStubCollector;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

public class MultiTicketResourceHandler implements TicketResourceHandler{

    private final List<TicketResourceHandler> handlers;
    private MultiTicketResourceHandler(List<TicketResourceHandler> handlers) { this.handlers = List.copyOf(handlers); }

    public static TicketResourceHandler combine(List<TicketResourceHandler> handlers) {
        if(handlers.isEmpty())
            return EmptyTicketlikeResourceHandler.INSTANCE;
        if(handlers.size() == 1)
            return handlers.getFirst();
        return new MultiTicketResourceHandler(handlers);
    }

    @Override
    public boolean hasUnlimitedUses(long ticketID) {
        return this.handlers.stream().anyMatch(h -> h.hasUnlimitedUses(ticketID));
    }

    @Override
    public long getCount(long ticketID) {
        long count = 0;
        for(TicketResourceHandler h : this.handlers)
            count += h.getCount(ticketID);
        return count;
    }

    @Override
    public int collectTicket(long ticketID, int count, TicketStubCollector stubCollector, TransactionContext transaction) {
        //If we have unlimited uses, don't attempt any collections
        if(this.hasUnlimitedUses(ticketID))
            return count;
        int collected = 0;
        for(TicketResourceHandler h : this.handlers) {
            collected += h.collectTicket(ticketID,count,stubCollector,transaction);
            if(collected >= count)
                return collected;
        }
        return collected;
    }

    @Override
    public int collectTicketStub(ItemStack destroyedTicket, int count, TransactionContext transaction) {
        int collected = 0;
        for(TicketResourceHandler h : this.handlers) {
            collected += h.collectTicketStub(destroyedTicket,count,transaction);
            if(collected >= count)
                return collected;
        }
        return collected;
    }

}