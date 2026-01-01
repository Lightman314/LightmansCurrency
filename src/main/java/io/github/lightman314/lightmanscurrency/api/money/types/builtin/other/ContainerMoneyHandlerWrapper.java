package io.github.lightman314.lightmanscurrency.api.money.types.builtin.other;

import io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.capability.money.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A basic {@link IMoneyHandler} container wrapper that allows interaction to the {@link CapabilityMoneyHandler} capabilities of all items within the container
 */
public class ContainerMoneyHandlerWrapper extends MoneyHandler implements Iterable<IMoneyHandler> {

    private final Container container;

    private final IClientTracker tracker;

    public ContainerMoneyHandlerWrapper(@Nonnull Container container, @Nonnull IClientTracker tracker) { this.container = container; this.tracker = Objects.requireNonNull(tracker); }

    @Nonnull
    @Override
    public Iterator<IMoneyHandler> iterator() { return new ContainerIterator(this.container,this.tracker); }

    @Nonnull
    @Override
    public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) {
        MoneyValue pending = insertAmount;
        for(IMoneyHandler handler : this)
        {
            pending = handler.insertMoney(pending,simulation);
            if(pending.isEmpty())
                return MoneyValue.empty();
        }
        return pending;
    }

    @Nonnull
    @Override
    public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation) {
        MoneyValue pending = extractAmount;
        for(IMoneyHandler handler : this)
        {
            pending = handler.extractMoney(pending,simulation);
            if(pending.isEmpty())
                return MoneyValue.empty();
        }
        return pending;
    }

    @Override
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) {
        for(IMoneyHandler handler : this)
        {
            if(handler.isMoneyTypeValid(value))
                return true;
        }
        return false;
    }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        for(IMoneyHandler handler : this)
            builder.merge(handler.getStoredMoney());
    }

    private static class ContainerIterator implements Iterator<IMoneyHandler>
    {

        private final Container container;
        private final IClientTracker parent;
        private int index = -1;
        ContainerIterator(@Nonnull Container container, @Nonnull IClientTracker parent) { this.container = container; this.parent = parent; }

        @Override
        public boolean hasNext() { return this.getNext(false) != null; }

        @Override
        public IMoneyHandler next() {
            IMoneyHandler next = this.getNext(true);
            if(next == null)
                throw new NoSuchElementException("Could not find the next IMoneyHandler within the container!");
            return next;
        }

        @Nullable
        private IMoneyHandler getNext(boolean update)
        {
            for(int i = this.index + 1; i < this.container.getContainerSize(); ++i)
            {
                ItemStack stack = this.container.getItem(i);
                IMoneyHandler handler = CapabilityMoneyHandler.getCapability(stack);
                if(handler != null)
                {
                    if(handler instanceof ISidedObject sided)
                        sided.flagAsClient(this.parent);
                    if(update)
                        this.index = i;
                    return handler;
                }
            }
            return null;
        }

    }

}