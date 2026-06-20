package io.github.lightman314.lightmanscurrency.api.money.resource.builtin;

import io.github.lightman314.lightmanscurrency.api.money.resource.DeferredMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

public final class ListenerWrapper extends SnapshotJournal<Unit> implements DeferredMoneyResourceHandler {

    private final MoneyResourceHandler handler;
    private final List<Runnable> listeners = new ArrayList<>();
    public ListenerWrapper(MoneyResourceHandler handler) { this.handler = handler; }

    public void addListener(Runnable listener) {
        if(!this.listeners.contains(listener))
            this.listeners.add(listener);
    }

    @Override
    public MoneyResourceHandler getMoneyResourceHandler() { return this.handler; }

    @Override
    public MoneyValue insert(MoneyValue value, TransactionContext transaction) {
        MoneyValue result = DeferredMoneyResourceHandler.super.insert(value, transaction);
        if(!result.isEmpty()) //Only add to the snapshots listener if something actually happened
            this.updateSnapshots(transaction);
        return result;
    }

    @Override
    public MoneyValue extract(MoneyValue value, TransactionContext transaction) {
        MoneyValue result = DeferredMoneyResourceHandler.super.extract(value, transaction);
        if(!result.isEmpty()) //Only add to the snapshots listener if something actually happened
            this.updateSnapshots(transaction);
        return result;
    }
    @Override
    protected Unit createSnapshot() { return Unit.INSTANCE; }
    @Override
    protected void revertToSnapshot(Unit snapshot) { }
    @Override
    protected void onRootCommit(Unit originalState) {
        for(Runnable l : new ArrayList<>(this.listeners))
            l.run();
    }

}
