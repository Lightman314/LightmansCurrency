package io.github.lightman314.lightmanscurrency.api.money;

import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface OverflowHandler {

    void overflowItem(ItemStack item,TransactionContext transaction);

    static OverflowHandler forPlayer(Player player) { return forPlayer(() -> player); }
    static OverflowHandler forPlayer(Supplier<Player> player) { return new Template.ForPlayer(player); }

    static OverflowHandler forConsumer(Consumer<ItemStack> consumer) { return forListConsumer(Template.ConsumerWrapper.convertConsumer(consumer)); }
    static OverflowHandler forListConsumer(Consumer<List<ItemStack>> consumer) { return new Template.ConsumerWrapper(consumer); }

    static OverflowHandler forResourceHandler(ResourceHandler<ItemResource> handler,OverflowHandler fallback) { return new ResourceHandlerWrapper(handler,fallback); }
    static UnaryOperator<OverflowHandler> resourceHandlerModifier(ResourceHandler<ItemResource> handler) { return o -> forResourceHandler(handler,o); }

    abstract class Template extends SnapshotJournal<List<ItemStack>> implements OverflowHandler {

        private List<ItemStack> overflowItems = new ArrayList<>();

        @Override
        public void overflowItem(ItemStack item, TransactionContext transaction) {
            if(item.isEmpty())
                return;
            this.updateSnapshots(transaction);
            this.overflowItems.add(item);
        }

        @Override
        protected List<ItemStack> createSnapshot() { return ItemHelper.copyList(this.overflowItems); }
        @Override
        protected void revertToSnapshot(List<ItemStack> snapshot) { this.overflowItems = snapshot; }

        @Override
        protected void onRootCommit(List<ItemStack> originalState) {
            this.afterCommit(this.overflowItems);
            this.overflowItems = new ArrayList<>();
        }

        protected abstract void afterCommit(List<ItemStack> overflowItems);

        private static final class ConsumerWrapper extends Template {
            private final Consumer<List<ItemStack>> consumer;
            private ConsumerWrapper(Consumer<List<ItemStack>> consumer) { this.consumer = consumer; }
            private static Consumer<List<ItemStack>> convertConsumer(Consumer<ItemStack> consumer) {
                return list -> list.forEach(consumer);
            }
            @Override
            protected void afterCommit(List<ItemStack> overflowItems) { this.consumer.accept(overflowItems); }
        }

        private static final class ForPlayer extends Template {
            private final Supplier<Player> player;
            private ForPlayer(Supplier<Player> player) { this.player = player; }
            @Override
            protected void afterCommit(List<ItemStack> overflowItems) {
                Inventory inventory = this.player.get().getInventory();
                for(ItemStack item : overflowItems)
                    inventory.placeItemBackInInventory(item);
            }
        }

    }

    class ResourceHandlerWrapper implements OverflowHandler {
        private final ResourceHandler<ItemResource> itemResources;
        private final OverflowHandler fallback;
        private ResourceHandlerWrapper(ResourceHandler<ItemResource> handler, OverflowHandler fallback) {
            this.itemResources = handler;
            this.fallback = fallback;
        }
        @Override
        public void overflowItem(ItemStack item,TransactionContext transaction) {
            if(item.isEmpty())
                return;
            try(Transaction tx = Transaction.open(transaction)) {
                ItemStack copy = item.copy();
                int fit = this.itemResources.insert(ItemResource.of(item),item.getCount(),tx);
                if(fit > 0 && fit <= item.getCount()) {
                    tx.commit();
                    copy.shrink(fit);
                    if(!copy.isEmpty())
                        this.fallback.overflowItem(copy,transaction);
                }
            }
        }
    }

}
