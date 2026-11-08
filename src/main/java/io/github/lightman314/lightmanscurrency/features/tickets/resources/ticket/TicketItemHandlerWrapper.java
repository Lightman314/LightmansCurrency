package io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket;

import io.github.lightman314.lightmanscurrency.features.tickets.TicketHelper;
import io.github.lightman314.lightmanscurrency.features.tickets.data.TicketUses;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub.StubItemHandlerWrapper;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub.TicketStubCollector;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Predicate;

public class TicketItemHandlerWrapper extends StubItemHandlerWrapper implements TicketResourceHandler {

    public TicketItemHandlerWrapper(ResourceHandler<ItemResource> storage) { super(storage); }

    @Override
    public boolean hasUnlimitedUses(long ticketID) {
        for(int i = 0; i < this.storage.size(); ++i) {
            ItemResource item = this.storage.getResource(i);
            if(TicketHelper.isConsumableTicket(item)) {
                if(item.get(LCDataComponents.TICKET_ID).ticketID() == ticketID && TicketUses.get(item).isUnlimited())
                    return true;
            }
        }
        return false;
    }

    @Override
    public long getCount(long ticketID) {
        long count = 0;
        for(int i = 0;i < this.storage.size();++i) {
            ItemResource item = this.storage.getResource(i);
            if(TicketHelper.isConsumableTicket(item)) {
                long id = item.get(LCDataComponents.TICKET_ID).ticketID();
                if(id == ticketID) {
                    long itemCount = this.storage.getAmountAsLong(i);
                    TicketUses uses = TicketUses.get(item);
                    if(uses.isUnlimited())
                        return Long.MAX_VALUE;
                    count += itemCount * uses.uses();
                }
            }
        }
        return count;
    }

    @Override
    public int collectTicket(long ticketID,int count,TicketStubCollector stubCollector,TransactionContext transaction) {
        if(this.hasUnlimitedUses(ticketID))
            return count;
        return consumeTicketLike(this.storage,item -> TicketHelper.isConsumableTicket(item,ticketID),count,stubCollector,transaction);
    }

    public static int consumeTicketLike(ResourceHandler<ItemResource> storage,Predicate<ItemResource> filter,int count,TicketStubCollector stubCollector,TransactionContext transaction) {
        int collected = 0;
        int remaining = count;
        for(int i = 0; i < storage.size(); ++i) {
            ItemResource item = storage.getResource(i);
            if(filter.test(item)) {
                TicketUses ticketUseData = TicketUses.get(item);
                int usesPerTicket = ticketUseData.uses();
                //Consume an entire ticket until we're unable to remove more
                while(remaining >= usesPerTicket) {
                    try(Transaction tx = Transaction.open(transaction)) {
                        int removed = storage.extract(i,item,1,tx);
                        if(removed == 1) {
                            //Spawn the ticket stub for the consumed ticket
                            if(stubCollector.collectTicketStub(item.toStack(),1,tx) == 1) {
                                remaining -= usesPerTicket;
                                collected += usesPerTicket;
                                tx.commit();
                            }
                            else
                                break;
                        }
                        else //Break the loop for this entry if we failed to remove the expected amount
                            break;
                    }
                }
                //Consume a partial ticket
                if(remaining < usesPerTicket && remaining > 0) {
                    //Extract the item
                    try(Transaction tx = Transaction.open(transaction)) {
                        int removed = storage.extract(i,item,1,tx);
                        if(removed == 1) {
                            //Insert a copy with partial durability
                            ItemResource damagedTicket = item.with(LCDataComponents.TICKET_USES,ticketUseData.consume(remaining));
                            int inserted = storage.insert(i,damagedTicket,1,tx);
                            boolean success = false;
                            if(inserted == 1)
                                success = true;
                            else if(inserted == 0) {
                                //If it doesn't fit in its original slot, instead place it in the next relevant slot
                                inserted = ResourceHandlerUtil.insertStacking(storage,damagedTicket,1,tx);
                                if(inserted == 1)
                                    success = true;
                            }
                            if(success) {
                                tx.commit();
                                collected += remaining;
                                return collected;
                            }
                        }
                    }
                }
            }
        }
        return collected;
    }

}