package io.github.lightman314.lightmanscurrency.api.money.types.builtin.other;

import io.github.lightman314.lightmanscurrency.api.money.capability.CapabilityMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.capability.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyViewer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

/**
 * A basic {@link IMoneyHandler} container wrapper that allows interaction to the {@link CapabilityMoneyHandler#MONEY_HANDLER_ITEM} capabilities of all items within the container
 */
public class ContainerMoneyHandlerWrapper extends MoneyHandler implements Iterable<IMoneyHandler> {

    private final IItemHandlerModifiable container;
    private final IClientTracker tracker;

    public ContainerMoneyHandlerWrapper(IItemHandlerModifiable container, IClientTracker tracker) { this.container = container; this.tracker = Objects.requireNonNull(tracker); }

    @Override
    public Iterator<IMoneyHandler> iterator() { return new ContainerIterator(this.container,this.tracker); }

    @Override
    public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) {
        MoneyValue pending = insertAmount;
        for(IMoneyHandler handler : this)
        {
            pending = handler.insertMoney(pending,simulation);
            if(pending.isEmpty())
                return MoneyValue.empty();
        }
        return pending;
    }

    @Override
    public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) {
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
    public boolean isMoneyTypeValid(MoneyValue value) {
        for(IMoneyHandler handler : this)
        {
            if(handler.isMoneyTypeValid(value))
                return true;
        }
        return false;
    }

    @Override
    protected void collectStoredMoney(MoneyView.Builder builder) {
        for(IMoneyHandler handler : this)
            builder.merge(handler.getStoredMoney());
    }

    private static class ContainerIterator implements Iterator<IMoneyHandler>
    {

        private final IItemHandlerModifiable container;
        private final IClientTracker parent;
        private int index = -1;
        ContainerIterator(IItemHandlerModifiable container, IClientTracker parent) { this.container = container; this.parent = parent; }

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
            for(int i = this.index + 1; i < this.container.getSlots(); ++i)
            {
                ItemStack stack = this.container.getStackInSlot(i).copy();
                IMoneyHandler handler = stack.getCapability(CapabilityMoneyHandler.MONEY_HANDLER_ITEM);
                if(handler != null)
                {
                    if(update)
                        this.index = i;
                    return new Wrapper(this.container,i,this.parent);
                }
            }
            return null;
        }

        private record Wrapper(IItemHandlerModifiable container,int index,IClientTracker context) implements IMoneyHandler
        {

            private <T> T wrapInteraction(Function<IMoneyHandler,T> interaction,T defaultValue)
            {
                ItemStack original = this.container.getStackInSlot(this.index);
                ItemStack stack = original.copy();
                IMoneyHandler handler = stack.getCapability(CapabilityMoneyHandler.MONEY_HANDLER_ITEM);
                if(handler == null)
                    return defaultValue;
                if(handler instanceof ISidedObject object)
                    object.flagAsClient(this.context);
                T result = interaction.apply(handler);
                if(!ItemStack.isSameItemSameComponents(original,stack) || original.getCount() != stack.getCount())
                    this.container.setStackInSlot(this.index,stack);
                return result;
            }

            @Override
            public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) {
                return this.wrapInteraction(handler ->
                        handler.insertMoney(insertAmount,simulation),insertAmount);
            }

            @Override
            public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation) {
                return this.wrapInteraction(handler ->
                        handler.extractMoney(extractAmount,simulation),extractAmount);
            }

            @Override
            public boolean isMoneyTypeValid(MoneyValue value) {
                return this.wrapInteraction(handler -> handler.isMoneyTypeValid(value),false);
            }

            @Override
            public MoneyView getStoredMoney() { return this.wrapInteraction(IMoneyViewer::getStoredMoney,MoneyView.empty()); }

        }

    }



}
