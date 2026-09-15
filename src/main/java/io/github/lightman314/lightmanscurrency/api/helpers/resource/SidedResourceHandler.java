package io.github.lightman314.lightmanscurrency.api.helpers.resource;

import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettingsState;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SidedResourceHandler<T extends Resource> implements ResourceHandler<T> {

    private final Supplier<ResourceHandler<T>> handler;
    private final Supplier<DirectionalSettingsState> stateSource;
    public SidedResourceHandler(ResourceHandler<T> handler,Supplier<DirectionalSettingsState> stateSource) { this(() -> handler,stateSource); }
    public SidedResourceHandler(Supplier<ResourceHandler<T>> handler,Supplier<DirectionalSettingsState> stateSource) {
        this.handler = handler;
        this.stateSource = stateSource;
    }

    private ResourceHandler<T> getHandler() {
        return Objects.requireNonNullElseGet(this.handler.get(),EmptyResourceHandler::instance);
    }

    @Override
    public int size() { return this.getHandler().size(); }
    @Override
    public T getResource(int index) { return this.getHandler().getResource(index); }
    @Override
    public long getAmountAsLong(int index) { return this.getHandler().getAmountAsLong(index); }
    @Override
    public long getCapacityAsLong(int index,T resource) { return this.getHandler().getCapacityAsLong(index,resource); }
    @Override //Don't acknowledge it as valid if inputs are not allowed
    public boolean isValid(int index,T resource) { return this.stateSource.get().allowsInputs() && this.getHandler().isValid(index,resource); }
    @Override
    public int insert(T resource,int amount,TransactionContext transaction) {
        if(this.stateSource.get().allowsInputs())
            return this.getHandler().insert(resource,amount,transaction);
        return 0;
    }
    @Override
    public int insert(int index,T resource,int amount,TransactionContext transaction) {
        if(this.stateSource.get().allowsInputs())
            return this.getHandler().insert(index,resource,amount,transaction);
        return 0;
    }

    @Override
    public int extract(T resource,int amount,TransactionContext transaction) {
        if(this.stateSource.get().allowsOutputs())
            return this.getHandler().extract(resource,amount,transaction);
        return 0;
    }

    @Override
    public int extract(int index,T resource,int amount,TransactionContext transaction) {
        if(this.stateSource.get().allowsOutputs())
            return this.getHandler().extract(index,resource,amount,transaction);
        return 0;
    }

    public static class WithExtractionRule<T extends Resource> extends SidedResourceHandler<T> {
        private final Predicate<T> extractionRule;
        public WithExtractionRule(ResourceHandler<T> handler,Supplier<DirectionalSettingsState> stateSource,Predicate<T> extractionRule) {
            super(handler, stateSource);
            this.extractionRule = extractionRule;
        }
        public WithExtractionRule(Supplier<ResourceHandler<T>> handler,Supplier<DirectionalSettingsState> stateSource,Predicate<T> extractionRule) {
            super(handler, stateSource);
            this.extractionRule = extractionRule;
        }
        @Override
        public int extract(T resource, int amount, TransactionContext transaction) {
            if(!this.extractionRule.test(resource))
                return 0;
            return super.extract(resource, amount, transaction);
        }
        @Override
        public int extract(int index, T resource, int amount, TransactionContext transaction) {
            if(!this.extractionRule.test(resource))
                return 0;
            return super.extract(index, resource, amount, transaction);
        }

    }

}