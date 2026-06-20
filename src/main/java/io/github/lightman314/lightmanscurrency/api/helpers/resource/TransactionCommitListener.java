package io.github.lightman314.lightmanscurrency.api.helpers.resource;

import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class TransactionCommitListener<T> extends SnapshotJournal<T> {

    private T data;
    private final Supplier<T> cleanData;
    private final UnaryOperator<T> copy;
    private final Consumer<T> onCommit;
    public TransactionCommitListener(Supplier<T> cleanData, UnaryOperator<T> copyData, Consumer<T> onCommit) {
        this.data = cleanData.get();
        this.cleanData = cleanData;
        this.copy = copyData;
        this.onCommit = onCommit;
    }

    public static BiConsumer<ItemStack,TransactionContext> wrapAction(Consumer<ItemStack> action)
    {
        final TransactionCommitListener<List<ItemStack>> listener =  new TransactionCommitListener<>(
                ArrayList::new,ItemHelper::copyList,list -> list.forEach(action));
        return (item,tx) -> {
            //Update the snapshot
            listener.updateSnapshots(tx);
            listener.data.add(item);
        };
    }

    @Override
    protected T createSnapshot() { return this.copy.apply(this.data); }

    @Override
    protected void revertToSnapshot(T snapshot) { this.data = this.copy.apply(snapshot); }

    @Override
    protected void onRootCommit(T originalState) {
        this.onCommit.accept(this.data);
        this.data = this.cleanData.get();
    }

}