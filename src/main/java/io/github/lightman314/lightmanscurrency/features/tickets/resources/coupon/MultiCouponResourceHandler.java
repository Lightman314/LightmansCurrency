package io.github.lightman314.lightmanscurrency.features.tickets.resources.coupon;

import io.github.lightman314.lightmanscurrency.features.tickets.resources.EmptyTicketlikeResourceHandler;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub.TicketStubCollector;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

public class MultiCouponResourceHandler implements CouponResourceHandler {

    private final List<CouponResourceHandler> handlers;
    private MultiCouponResourceHandler(List<CouponResourceHandler> handlers) { this.handlers = List.copyOf(handlers); }

    public static CouponResourceHandler combine(List<CouponResourceHandler> handlers) {
        if(handlers.isEmpty())
            return EmptyTicketlikeResourceHandler.INSTANCE;
        if(handlers.size() == 1)
            return handlers.getFirst();
        return new MultiCouponResourceHandler(handlers);
    }

    @Override
    public boolean hasUnlimitedUses(String code) { return this.handlers.stream().anyMatch(h -> h.hasUnlimitedUses(code)); }

    @Override
    public long getCount(String code) {
        long count = 0;
        for(CouponResourceHandler handler : this.handlers)
            count += handler.getCount(code);
        return count;
    }

    @Override
    public int collectCoupon(String code,int count,TicketStubCollector stubCollector,TransactionContext transaction) {
        if(this.hasUnlimitedUses(code))
            return count;
        int collected = 0;
        for(CouponResourceHandler handler : this.handlers) {
            collected += handler.collectCoupon(code,count,stubCollector,transaction);
            if(collected >= count)
                return collected;
        }
        return collected;
    }

    @Override
    public int collectTicketStub(ItemStack destroyedTicket, int count, TransactionContext transaction) {
        int collected = 0;
        for(CouponResourceHandler handler : this.handlers) {
            collected += handler.collectTicketStub(destroyedTicket,count,transaction);
            if(collected >= count)
                return collected;
        }
        return collected;
    }

}
