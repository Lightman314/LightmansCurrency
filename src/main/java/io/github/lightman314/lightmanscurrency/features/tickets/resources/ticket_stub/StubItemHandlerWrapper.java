package io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub;

import io.github.lightman314.lightmanscurrency.features.tickets.TicketHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class StubItemHandlerWrapper implements TicketStubCollector {

    protected final ResourceHandler<ItemResource> storage;
    public StubItemHandlerWrapper(ResourceHandler<ItemResource> storage) { this.storage = storage; }

    @Override
    public int collectTicketStub(ItemStack ticket,int count,TransactionContext transaction) {
        Item stub = TicketHelper.getTicketStub(ticket);
        if(stub != null)
            return this.storage.insert(ItemResource.of(stub),count,transaction);
        return 0;
    }
}
