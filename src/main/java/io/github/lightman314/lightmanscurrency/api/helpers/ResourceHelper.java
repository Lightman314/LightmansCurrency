package io.github.lightman314.lightmanscurrency.api.helpers;

import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class ResourceHelper {

    private ResourceHelper() {}

    public static <T extends Resource> long getResourceCount(ResourceHandler<T> handler,Predicate<T> filter,@Nullable TransactionContext transaction)
    {
        long count = 0;
        for(int i = 0; i < handler.size(); ++i)
        {
            T resource = handler.getResource(i);
            if(filter.test(resource))
                count += handler.extract(i,resource,Integer.MAX_VALUE,Transaction.open(transaction));
        }
        return count;
    }

    /**
     * Loops {@link ResourceHandlerUtil#extractFirst(ResourceHandler, Predicate, int, TransactionContext)} until the targeted amount has been extracted or the extraction fails.
     * @param handler The resource handler to extract from
     * @param filter The filter for what resource to extract
     * @param extractAmount The maximum amount of the resource to extract
     * @param transaction The transaction
     * @return The quantity extracted
     * @param <T> The type of {@link Resource} to be extracted
     */
    public static <T extends Resource> ExtractionResults<T> extractFirstToTarget(ResourceHandler<T> handler, Predicate<T> filter, int extractAmount, @Nullable TransactionContext transaction)
    {
        //Create a sub-transation for
        int count = 0;
        List<ResourceStack<T>> extracted = new ArrayList<>();
        while(count < extractAmount)
        {
            try(Transaction tx = Transaction.open(transaction)) {
                ResourceStack<T> stack = ResourceHandlerUtil.extractFirst(handler,filter,extractAmount - count,tx);
                if(stack == null || stack.isEmpty())
                    return new ExtractionResults<>(extracted,count);
                count += stack.amount();
                extracted.add(stack);
                tx.commit();
            }
        }
        return new ExtractionResults<>(extracted,count);
    }

    public static <T extends Resource> ExtractionResults<T> extractRandomToTarget(ResourceHandler<T> handler,Predicate<T> filter,int extractAmount,RandomSource random,@Nullable TransactionContext transaction) {
        int count = 0;
        List<ResourceStack<T>> extracted = new ArrayList<>();
        while(count < extractAmount)
        {
            T removed = extractRandom(handler,filter,random,transaction);
            if(removed == null) //Stop if we failed to find anything to extract
                break;
            else
            {
                extracted.add(new ResourceStack<>(removed,1));
                count++;
            }
        }
        return new ExtractionResults<>(ResourceHelper.mergeResources(extracted),count);
    }

    @Nullable
    public static <T extends Resource> T extractRandom(ResourceHandler<T> handler, Predicate<T> filter,RandomSource random,@Nullable TransactionContext transaction)
    {
        try(Transaction tx = Transaction.open(transaction)) {
            WeightedList.Builder<T> optionsBuilder = WeightedList.builder();
            for(int i = 0; i < handler.size(); ++i)
            {
                T entry = handler.getResource(i);
                if(!entry.isEmpty() && filter.test(entry))
                    optionsBuilder.add(entry,handler.getAmountAsInt(i));
            }
            WeightedList<T> list = optionsBuilder.build();
            if(list.isEmpty()) //If nothing is in the list, then we failed to extract anything
                return null;
            T toTake = list.getRandomOrThrow(random);
            int taken  = handler.extract(toTake,1,tx);
            if(taken == 1)
            {
                tx.commit();
                return toTake;
            }
        }
        return null;
    }

    public static <T extends Resource> List<ResourceStack<T>> mergeResources(List<ResourceStack<T>> list) {
        List<ResourceStack<T>> result = new ArrayList<>();
        for(ResourceStack<T> s : list)
        {
            if(s.isEmpty())
                continue;
            boolean add = true;
            for(int i = 0; add && i < result.size(); ++i)
            {
                ResourceStack<T> r = result.get(i);
                if(s.resource().equals(r.resource()))
                {
                    result.set(i,new ResourceStack<>(s.resource(),s.amount() + r.amount()));
                    add = false;
                }
            }
            if(add)
                result.add(s);
        }
        return result;
    }

    public record ExtractionResults<T extends Resource>(List<ResourceStack<T>> extracted, int totalCount){ }

}
