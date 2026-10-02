package io.github.lightman314.lightmanscurrency.api.money.resource.builtin;

import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettingsState;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;
import java.util.function.Supplier;

public class SidedMoneyResourceWrapper implements MoneyResourceHandler {

    private final MoneyResourceHandler handler;
    private final Supplier<DirectionalSettingsState> stateSource;
    public SidedMoneyResourceWrapper(MoneyResourceHandler handler,Supplier<DirectionalSettingsState> stateSource) {
        this.handler = handler;
        this.stateSource = stateSource;
    }
    @Override
    public List<MoneyValue> getAllResources() { return this.handler.getAllResources(); }
    @Override
    public MoneyValue getResource(DualKey key) { return this.handler.getResource(key); }

    @Override
    public MoneyValue insert(MoneyValue value,TransactionContext transaction) {
        if(this.stateSource.get().allowsInputs())
            return this.handler.insert(value,transaction);
        return MoneyValue.empty();
    }

    @Override
    public MoneyValue extract(MoneyValue value,TransactionContext transaction) {
        if(this.stateSource.get().allowsOutputs())
            return this.handler.extract(value,transaction);
        return MoneyValue.empty();
    }

}
