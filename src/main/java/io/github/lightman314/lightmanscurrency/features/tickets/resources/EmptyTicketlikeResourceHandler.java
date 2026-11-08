package io.github.lightman314.lightmanscurrency.features.tickets.resources;

import io.github.lightman314.lightmanscurrency.features.tickets.resources.coupon.CouponResourceHandler;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket.TicketResourceHandler;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub.TicketStubCollector;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class EmptyTicketlikeResourceHandler implements TicketResourceHandler, CouponResourceHandler {

    public static final EmptyTicketlikeResourceHandler INSTANCE = new EmptyTicketlikeResourceHandler();

    private EmptyTicketlikeResourceHandler() {}

    @Override
    public boolean hasUnlimitedUses(long ticketID) { return false; }
    @Override
    public long getCount(long ticketID) { return 0; }
    @Override
    public int collectTicket(long ticketID, int count, TicketStubCollector stubCollector, TransactionContext transaction) { return 0; }
    @Override
    public int collectTicketStub(ItemStack destroyedTicket, int count, TransactionContext transaction) { return 0; }
    @Override
    public boolean hasUnlimitedUses(String code) { return false; }
    @Override
    public long getCount(String code) { return 0; }
    @Override
    public int collectCoupon(String code, int count, TicketStubCollector stubCollector, TransactionContext transaction) { return 0; }

}