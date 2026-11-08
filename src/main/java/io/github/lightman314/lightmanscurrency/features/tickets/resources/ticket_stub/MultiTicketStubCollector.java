package io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub;

import io.github.lightman314.lightmanscurrency.features.tickets.resources.EmptyTicketlikeResourceHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

public class MultiTicketStubCollector implements TicketStubCollector {

    private final List<TicketStubCollector> collectors;
    private MultiTicketStubCollector(List<TicketStubCollector> collectors) { this.collectors = List.copyOf(collectors); }

    public static TicketStubCollector combine(List<TicketStubCollector> collectors) {
        if(collectors.isEmpty())
            return EmptyTicketlikeResourceHandler.INSTANCE;
        if(collectors.size() == 1)
            return collectors.getFirst();
        return new MultiTicketStubCollector(collectors);
    }

    @Override
    public int collectTicketStub(ItemStack destroyedTicket, int count, TransactionContext transaction) {
        int collected = 0;
        for(TicketStubCollector c : this.collectors) {
            collected += c.collectTicketStub(destroyedTicket,count,transaction);
            if(collected >= count)
                return collected;
        }
        return collected;
    }

}