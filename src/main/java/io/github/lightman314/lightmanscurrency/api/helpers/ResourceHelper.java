package io.github.lightman314.lightmanscurrency.api.helpers;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public final class ResourceHelper {

    private ResourceHelper() {}

    public static <T extends Resource> long getResourceCount(ResourceHandler<T> handler,Predicate<T> filter,@Nullable Transaction transaction)
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
    public static <T extends Resource> int extractFirstToTarget(ResourceHandler<T> handler, Predicate<T> filter, int extractAmount, @Nullable Transaction transaction)
    {
        //Create a sub-transation for
        int count = 0;
        while(count < extractAmount)
        {
            try(Transaction tx = Transaction.open(transaction)) {
                ResourceStack<T> stack = ResourceHandlerUtil.extractFirst(handler,filter,extractAmount - count,tx);
                if(stack == null || stack.isEmpty())
                    return count;
                count += stack.amount();
                tx.commit();
            }
        }
        return count;
    }

}
