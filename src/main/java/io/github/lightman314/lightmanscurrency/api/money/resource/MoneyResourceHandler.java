package io.github.lightman314.lightmanscurrency.api.money.resource;


import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

/**
 * Similar to neo's {@link net.neoforged.neoforge.transfer.ResourceHandler ResourceHandler}, but more streamlined for monetary transations as slots don't need to be considered<br>
 *
 */
public interface MoneyResourceHandler {

    static MoneyResourceHandler viewOnly(MoneyResourceHandler handler) { return new ViewOnly.Slave(handler); }

    /**
     * A collection of all {@link MoneyValue}'s contained within this Money Resource Handler<br>
     * List will be empty if no money is available
     */
    List<MoneyValue> getAllResources();

    /**
     * The currently available {@link MoneyValue} that matches the given {@link MoneyKey}<br>
     * Is {@link MoneyValue#empty()} if no money of that type is available
     */
    MoneyValue getResource(MoneyKey key);

    /**
     * Whether this Money Resource Handler is completely empty
     */
    default boolean isEmpty() {
        List<MoneyValue> contents = this.getAllResources();
        return contents.isEmpty() || contents.stream().allMatch(MoneyValue::isEmpty);
    }

    /**
     * Whether the given amount of money is currently stored within this resource handler
     */
    default boolean containsResource(MoneyValue value) { return this.getResource(value.getKey()).containsValue(value); }

    /**
     * Returns the maximum amount of money that can be extracted of the given {@link MoneyKey}
     */
    default MoneyValue capValue(MoneyValue value) { return this.containsResource(value) ? value : this.getResource(value.getKey()); }

    /**
     * Inserts up to the given amount of Money into the handler.
     *
     * <p>Changes to the handler are made in the context of a {@linkplain Transaction transaction}.
     *
     * @param value The maximum amount of Money to insert
     * @param transaction The transaction that this operation is part of.
     * @return The {@link MoneyValue} of the money that was inserted.
     * Between {@link MoneyValue#empty()} (inclusive, nothing was inserted) and {@code value} (inclusive, everything was inserted).
     * @implSpec Implementations must properly support {@linkplain Transaction transactions}.
     *           Note that {@link net.neoforged.neoforge.transfer.transaction.SnapshotJournal SnapshotJournal} can serve as the base class for a transaction-aware resource handler.
     */
    MoneyValue insert(MoneyValue value, TransactionContext transaction);

    /**
     * Extracts up to the given amount of Money from the handler.
     *
     * <p>Changes to the handler are made in the context of a {@linkplain Transaction transaction}.
     *
     * @param value The maximum amount of Money to extract
     * @param transaction The transaction that this operation is part of.
     * @return The {@link MoneyValue} of the money that was extracted.
     * Between {@linkplain MoneyValue#empty()} (inclusive, nothing was extraced) and {@code value} (inclusive, everything was extracted).
     * @implSpec Implementations must properly support {@linkplain Transaction transactions}.
     *           Note that {@link net.neoforged.neoforge.transfer.transaction.SnapshotJournal SnapshotJournal} can serve as the base class for a transaction-aware resource handler.
     */
    MoneyValue extract(MoneyValue value, TransactionContext transaction);

    interface Slave extends MoneyResourceHandler
    {

        MoneyResourceHandler getMoneyResourceHandler();
        @Override
        default List<MoneyValue> getAllResources() { return this.getMoneyResourceHandler().getAllResources(); }
        @Override
        default MoneyValue getResource(MoneyKey key) { return this.getMoneyResourceHandler().getResource(key); }
        @Override
        default boolean isEmpty() { return this.getMoneyResourceHandler().isEmpty(); }
        @Override
        default boolean containsResource(MoneyValue value) { return this.getMoneyResourceHandler().containsResource(value); }
        @Override
        default MoneyValue capValue(MoneyValue value) { return this.getMoneyResourceHandler().capValue(value); }
        @Override
        default MoneyValue insert(MoneyValue value, TransactionContext transaction) { return this.getMoneyResourceHandler().insert(value,transaction); }
        @Override
        default MoneyValue extract(MoneyValue value, TransactionContext transaction) { return this.getMoneyResourceHandler().extract(value,transaction); }
    }

    abstract class ViewOnly implements MoneyResourceHandler
    {
        @Override
        public final MoneyValue insert(MoneyValue value, TransactionContext transaction) { return MoneyValue.empty(); }
        @Override
        public final MoneyValue extract(MoneyValue value, TransactionContext transaction) { return MoneyValue.empty(); }

        private static final class Slave extends ViewOnly
        {
            private final MoneyResourceHandler handler;
            private Slave(MoneyResourceHandler handler) { this.handler = handler; }

            @Override
            public List<MoneyValue> getAllResources() { return this.handler.getAllResources(); }
            @Override
            public MoneyValue getResource(MoneyKey key) { return this.handler.getResource(key); }
            @Override
            public boolean isEmpty() { return this.handler.isEmpty(); }
            @Override
            public boolean containsResource(MoneyValue value) { return this.handler.containsResource(value); }
            @Override
            public MoneyValue capValue(MoneyValue value) { return this.handler.capValue(value); }
        }
    }

}
