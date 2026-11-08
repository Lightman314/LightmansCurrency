package io.github.lightman314.lightmanscurrency.features.tickets.resources.coupon;

import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub.TicketStubCollector;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public interface CouponResourceHandler extends TicketStubCollector {

    boolean hasUnlimitedUses(String code);
    default boolean hasCode(String code) { return this.getCount(code) > 0; }
    long getCount(String code);

    default int collectCoupon(String code,int count,TransactionContext transaction) { return this.collectCoupon(code,count,this,transaction); }
    int collectCoupon(String code,int count,TicketStubCollector stubCollector,TransactionContext transaction);

}