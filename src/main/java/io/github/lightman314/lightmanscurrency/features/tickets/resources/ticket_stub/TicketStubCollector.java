package io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public interface TicketStubCollector {

    int collectTicketStub(ItemStack destroyedTicket,int count,TransactionContext transaction);

}
