package io.github.lightman314.lightmanscurrency.features.trader.item.requirements;

import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ResourceHelper;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class ItemRequirement implements Predicate<ItemResource> {

    private int count;
    public ItemRequirement(int count) { this.count = count; }

    public final int getCount() { return this.count; }
    public final void grow(int count) { this.count += count; }
    public boolean isEmpty() { return this.getDummyResource().isEmpty() || this.count <= 0; }
    public abstract ItemRequirement copy();

    public ResourceStack<ItemResource> getDummyStack() { return new ResourceStack<>(this.getDummyResource(),this.count); }
    protected abstract ItemResource getDummyResource();

    public abstract List<ResourceStack<ItemResource>> getRandomResources(RandomSource random);

    public abstract boolean canMerge(ItemRequirement other);

    public static List<ItemRequirement> combine(List<ItemRequirement> requirements) {
        List<ItemRequirement> result = new ArrayList<>();
        if(requirements.isEmpty())
            return result;
        if(requirements.size() == 1)
            return requirements;
        for(ItemRequirement r : requirements) {
            if(r.isEmpty())
                continue;
            boolean add = true;
            for(ItemRequirement present : result) {
                if(add && present.canMerge(r)) {
                    present.grow(r.getCount());
                    add = false;
                }
            }
            if(add)
                result.add(r.copy());
        }
        return result;
    }

    public abstract static class SingleResult extends ItemRequirement {
        public SingleResult(int count) { super(count); }
        @Override
        public List<ResourceStack<ItemResource>> getRandomResources(RandomSource random)  { return List.of(this.getDummyStack()); }
    }

    public abstract static class MultiResult extends ItemRequirement {
        public MultiResult(int count) { super(count); }
        protected abstract List<Holder<Item>> getPossibleResults();
        @Override
        protected ItemResource getDummyResource() {
            List<Holder<Item>> items = this.getPossibleResults();
            return items.isEmpty() ? ItemResource.EMPTY : ItemResource.of(items.getFirst());
        }
        @Override
        public List<ResourceStack<ItemResource>> getRandomResources(RandomSource random) {
            List<Holder<Item>> items = this.getPossibleResults();
            if(items.isEmpty())
                return List.of();
            List<ItemStack> results = new ArrayList<>();
            for(int i = 0; i < this.getCount(); ++i)
                results.add(new ItemStack(items.get(random.nextInt(items.size()))));
            results = ItemHelper.combineStacks(results);
            return results.stream().map(s -> new ResourceStack<>(ItemResource.of(s),s.count())).toList();
        }
    }

    public final List<ResourceStack<ItemResource>> extractFromUnlimitedResources(ResourceHandler<ItemResource> potentialStorage, RandomSource random, @Nullable TransactionContext transaction) {
        if(ResourceHelper.getResourceCount(potentialStorage,this,transaction) > 0) {
            List<ResourceStack<ItemResource>> result = new ArrayList<>();
            //If at least one item matches this filter, then simply get a random item X amount of times, but without actually consuming the items
            for(int i = 0; i < this.count; ++i) {
                try(Transaction tx = Transaction.open(transaction)) {
                    ItemResource r = ResourceHelper.extractRandom(potentialStorage,this,random,transaction);
                    if(r == null) {
                        result = null;
                        break;
                    }
                    else
                        result.add(new ResourceStack<>(r,1));
                }
            }
            if(result != null)
                return ResourceHelper.mergeResources(result);
        }
        //Otherwise get a random default item
        return this.getRandomResources(random);
    }

}