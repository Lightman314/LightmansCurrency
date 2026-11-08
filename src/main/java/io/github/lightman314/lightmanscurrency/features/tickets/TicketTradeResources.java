package io.github.lightman314.lightmanscurrency.features.tickets;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.coupon.CouponItemHandlerWrapper;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.coupon.CouponResourceHandler;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.coupon.MultiCouponResourceHandler;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.EmptyTicketlikeResourceHandler;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket.MultiTicketResourceHandler;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket.TicketItemHandlerWrapper;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket.TicketResourceHandler;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub.MultiTicketStubCollector;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub.TicketStubCollector;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.ResourceWrapper;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

public final class TicketTradeResources {

    private TicketTradeResources() {}

    public static final ResourceWrapper<ResourceHandler<ItemResource>,TicketResourceHandler> TICKET_WRAPPER = new TicketResourceWrapper();
    public static final ResourceWrapper<ResourceHandler<ItemResource>,CouponResourceHandler> COUPON_WRAPPER = new CouponResourceWrapper();

    public static final ResourceType<CouponResourceHandler,CouponResourceHandler> COUPON_HANDLER = new CouponResourceType();
    public static final ResourceType<TicketResourceHandler,TicketResourceHandler> TICKET_HANDLER = new TicketResourceType();
    public static final ResourceType<TicketStubCollector,TicketStubCollector> STUB_COLLECTOR = new TicketStubResourceType();

    private static class TicketResourceType extends ResourceType<TicketResourceHandler,TicketResourceHandler> {
        @Override
        public Identifier getType() { return LCApi.id("ticket_resource"); }
        @Override
        public TicketResourceHandler empty() { return EmptyTicketlikeResourceHandler.INSTANCE; }
        @Override
        public TicketResourceHandler combine(List<TicketResourceHandler> resources) { return MultiTicketResourceHandler.combine(resources); }
    }

    private static class CouponResourceType extends ResourceType<CouponResourceHandler,CouponResourceHandler> {

        @Override
        public Identifier getType() { return LCApi.id("coupon_resource"); }
        @Override
        public CouponResourceHandler empty() { return EmptyTicketlikeResourceHandler.INSTANCE; }
        @Override
        public CouponResourceHandler combine(List<CouponResourceHandler> resources) { return MultiCouponResourceHandler.combine(resources); }

    }

    private static class TicketStubResourceType extends ResourceType<TicketStubCollector,TicketStubCollector> {

        @Override
        public Identifier getType() { return LCApi.id("ticket"); }
        @Override
        public TicketStubCollector empty() { return EmptyTicketlikeResourceHandler.INSTANCE; }
        @Override
        public TicketStubCollector combine(List<TicketStubCollector> resources) { return MultiTicketStubCollector.combine(resources); }

    }

    private static class TicketResourceWrapper extends ResourceWrapper<ResourceHandler<ItemResource>,TicketResourceHandler> {
        @Override
        public ResourceType<?,ResourceHandler<ItemResource>> getWrappedResource() { return BuiltInResourceTypes.ITEM; }
        @Override
        public ResourceType<TicketResourceHandler,TicketResourceHandler> getResultingResource() { return TICKET_HANDLER; }
        @Override
        public TicketResourceHandler wrapResource(TradeContext context,ResourceHandler<ItemResource> result) { return new TicketItemHandlerWrapper(result); }
    }

    private static class CouponResourceWrapper extends ResourceWrapper<ResourceHandler<ItemResource>,CouponResourceHandler> {
        @Override
        public ResourceType<?, ResourceHandler<ItemResource>> getWrappedResource() { return BuiltInResourceTypes.ITEM; }
        @Override
        public ResourceType<CouponResourceHandler, CouponResourceHandler> getResultingResource() { return COUPON_HANDLER; }
        @Override
        public CouponResourceHandler wrapResource(TradeContext context,ResourceHandler<ItemResource> result) { return new CouponItemHandlerWrapper(result,TicketHelper.getCodeKey(context)); }
    }

}