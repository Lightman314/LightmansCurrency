package io.github.lightman314.lightmanscurrency.features.tickets.resources.coupon;

import io.github.lightman314.lightmanscurrency.features.tickets.TicketHelper;
import io.github.lightman314.lightmanscurrency.features.tickets.data.TicketUses;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket.TicketItemHandlerWrapper;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub.StubItemHandlerWrapper;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub.TicketStubCollector;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class CouponItemHandlerWrapper extends StubItemHandlerWrapper implements CouponResourceHandler {

    private final String key;
    public CouponItemHandlerWrapper(ResourceHandler<ItemResource> storage,String key) {
        super(storage);
        this.key = key;
    }

    @Override
    public boolean hasUnlimitedUses(String code) {
        for(int i = 0; i < this.storage.size(); ++i) {
            ItemResource item = this.storage.getResource(i);
            if(TicketHelper.isCoupon(item,code,this.key) && TicketUses.get(item).isUnlimited())
                return true;
        }
        return false;
    }

    @Override
    public long getCount(String code) {
        long count = 0;
        for(int i = 0; i < this.storage.size(); ++i) {
            ItemResource item = this.storage.getResource(i);
            if(TicketHelper.isCoupon(item,this.key,code)) {
                TicketUses uses = item.getOrDefault(LCDataComponents.TICKET_USES,TicketUses.DEFAULT);
                if(uses.isUnlimited())
                    return Long.MAX_VALUE;
                count += this.storage.getAmountAsLong(i) * uses.uses();
            }
        }
        return count;
    }

    @Override
    public int collectCoupon(String code, int count, TicketStubCollector stubCollector, TransactionContext transaction) {
        if(hasUnlimitedUses(code))
            return count;
        return TicketItemHandlerWrapper.consumeTicketLike(this.storage,item -> TicketHelper.isCoupon(item,code,this.key),count,stubCollector,transaction);
    }

}
