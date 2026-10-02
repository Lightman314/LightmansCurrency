package io.github.lightman314.lightmanscurrency.api.money.resource.builtin;

import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class MoneyResourceHandlerCommitListenerWrapper extends SnapshotJournal<Void> implements MoneyResourceHandler.Slave {

    private final MoneyResourceHandler handler;
    private final Runnable listener;
    public MoneyResourceHandlerCommitListenerWrapper(MoneyResourceHandler handler,Runnable listener) {
        this.handler = handler;
        this.listener = listener;
    }

    @Override
    public MoneyResourceHandler getMoneyResourceHandler() {
        return this.handler;
    }

    @Override
    public MoneyValue insert(MoneyValue value, TransactionContext transaction) {
        MoneyValue result = Slave.super.insert(value, transaction);
        if(!result.isEmpty())
            this.updateSnapshots(transaction);
        return result;
    }

    @Override
    public MoneyValue extract(MoneyValue value, TransactionContext transaction) {
        MoneyValue result = Slave.super.extract(value,transaction);
        if(!result.isEmpty())
            this.updateSnapshots(transaction);
        return result;
    }

    @Override
    protected Void createSnapshot() { return null; }
    @Override
    protected void revertToSnapshot(Void snapshot) { }
    @Override
    protected void onRootCommit(Void originalState) { this.listener.run(); }

}
