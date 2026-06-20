package io.github.lightman314.lightmanscurrency.api.money.resource.builtin;

import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

public class EmptyMoneyResource implements MoneyResourceHandler {

    public static final MoneyResourceHandler INSTANCE = new EmptyMoneyResource();

    @Override
    public List<MoneyValue> getAllResources() { return List.of(); }
    @Override
    public MoneyValue getResource(MoneyKey key) { return MoneyValue.empty(); }
    @Override
    public MoneyValue insert(MoneyValue value,TransactionContext transaction) { return MoneyValue.empty(); }
    @Override
    public MoneyValue extract(MoneyValue value, TransactionContext transaction) { return MoneyValue.empty(); }

}