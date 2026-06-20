package io.github.lightman314.lightmanscurrency.api.money.resource;

import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * A {@link MoneyResourceHandler} extension that defers all methods to the handler provided by {@link #getMoneyResourceHandler()}
 */
public interface DeferredMoneyResourceHandler extends MoneyResourceHandler {

    MoneyResourceHandler getMoneyResourceHandler();
    @Override
    @ApiStatus.NonExtendable
    default List<MoneyValue> getAllResources() { return this.getMoneyResourceHandler().getAllResources(); }
    @Override
    @ApiStatus.NonExtendable
    default MoneyValue getResource(MoneyKey key) { return this.getMoneyResourceHandler().getResource(key); }
    @Override
    @ApiStatus.NonExtendable
    default boolean isEmpty() { return this.getMoneyResourceHandler().isEmpty(); }
    @Override
    @ApiStatus.NonExtendable
    default boolean containsResource(MoneyValue value) { return this.getMoneyResourceHandler().containsResource(value); }
    @Override
    @ApiStatus.NonExtendable
    default MoneyValue capValue(MoneyValue value) { return this.getMoneyResourceHandler().capValue(value); }
    @Override
    @ApiStatus.NonExtendable
    default MoneyValue insert(MoneyValue value, TransactionContext transaction) { return this.getMoneyResourceHandler().insert(value,transaction); }
    @Override
    @ApiStatus.NonExtendable
    default MoneyValue extract(MoneyValue value, TransactionContext transaction) { return this.getMoneyResourceHandler().extract(value,transaction); }

}