package io.github.lightman314.lightmanscurrency.api.money.resource.builtin;

import io.github.lightman314.lightmanscurrency.api.LCCapabilities;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.capabilities.SidedItemAccess;
import io.github.lightman314.lightmanscurrency.api.money.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ItemCapabilityResourceWrapper extends IterableMoneyResourceHandler implements Iterable<MoneyResourceHandler> {

    private final ResourceHandler<ItemResource> resourceHandler;
    private final ISidedContext context;

    public ItemCapabilityResourceWrapper(ResourceHandler<ItemResource> resourceHandler,ISidedContext context)
    {
        this.resourceHandler = resourceHandler;
        this.context = context;
    }

    @Override
    public List<MoneyValue> getAllResources() { return MoneyView.wrap(this).getAllResources(); }

    @Override
    public MoneyValue getResource(MoneyKey key) { return MoneyView.wrap(this).getResource(key); }

    @Override
    public Iterator<MoneyResourceHandler> iterator() { return new Loop(this.resourceHandler,this.context); }

    @Override
    protected Iterable<? extends MoneyResourceHandler> insertIterable() { return this; }
    @Override
    protected Iterable<? extends MoneyResourceHandler> extractIterable() { return this; }

    private static class Loop implements Iterator<MoneyResourceHandler>
    {
        private final ResourceHandler<ItemResource> itemResourceHandler;
        private final ISidedContext context;
        private int index = -1;
        private Loop(ResourceHandler<ItemResource> itemResourceHandler,ISidedContext context) { this.itemResourceHandler = itemResourceHandler; this.context = context; }

        @Override
        public boolean hasNext() { return this.getNext(false) != null; }

        @Override
        public MoneyResourceHandler next() {
            MoneyResourceHandler next = this.getNext(true);
            if(next == null)
                throw new NoSuchElementException("Could not find the next Money Resource Handler within the Item Resource Handler!");
            return next;
        }

        @Nullable
        private MoneyResourceHandler getNext(boolean update)
        {
            for(int i = this.index + 1; i < this.itemResourceHandler.size(); ++i)
            {
                SidedItemAccess access = SidedItemAccess.forHandlerIndex(this.itemResourceHandler,i,this.context);
                MoneyResourceHandler handler = access.getSidedCapability(LCCapabilities.Money.ITEM);
                if(handler != null)
                {
                    if(update)
                        this.index = i;
                    return handler;
                }
            }
            return null;
        }

    }

}
