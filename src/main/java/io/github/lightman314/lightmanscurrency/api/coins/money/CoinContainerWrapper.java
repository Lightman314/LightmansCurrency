package io.github.lightman314.lightmanscurrency.api.coins.money;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.coins.value.CoinValue;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class CoinContainerWrapper implements MoneyResourceHandler {

    private final ResourceHandler<ItemResource> itemResource;
    private final BiConsumer<ItemStack,TransactionContext> overflowHandler;
    public CoinContainerWrapper(ResourceHandler<ItemResource> itemResource,BiConsumer<ItemStack,TransactionContext> overflowHandler)
    {
        this.itemResource = itemResource;
        this.overflowHandler = overflowHandler;
    }

    @Override
    public List<MoneyValue> getAllResources() {
        //Look up all coins in the
        Map<String,Long> results = new HashMap<>();
        for(int i = 0; i < this.itemResource.size(); ++i)
        {
            ItemResource resource = this.itemResource.getResource(i);
            if(!resource.isEmpty())
            {
                ChainData chain = LCApi.getCoinAPI().lookupChain(resource);
                if(chain != null)
                {
                    CoinEntry entry = chain.findEntry(resource);
                    if(entry != null)
                    {
                        long foundValue = results.getOrDefault(chain.chain,0L);
                        //Use direct amount, as we don't want to risk a transaction here
                        foundValue += entry.getInternalValue() * this.itemResource.getAmountAsLong(i);
                        results.put(chain.chain,foundValue);
                    }
                }
            }
        }
        ImmutableList.Builder<MoneyValue> list = ImmutableList.builderWithExpectedSize(results.size());
        results.forEach((chain,value) -> list.add(CoinValue.fromNumber(chain,value)));
        return list.build();
    }

    @Override
    public MoneyValue getResource(MoneyKey key) {
        if(key.isType(CoinValue.TYPE))
        {
            ChainData chain = LCApi.getCoinAPI().lookupChain(key.getKey());
            if(chain == null)
                return MoneyValue.empty();
            long foundValue = 0;
            for(int i = 0; i < this.itemResource.size(); ++i)
            {
                ItemResource resource = this.itemResource.getResource(i);
                if(!resource.isEmpty())
                {
                    CoinEntry entry = chain.findEntry(resource);
                    if(entry != null)
                        foundValue += entry.getInternalValue() * this.itemResource.getAmountAsLong(i);
                }
            }
            return CoinValue.fromNumber(chain,foundValue);
        }
        return MoneyValue.empty();
    }

    @Override
    public MoneyValue insert(MoneyValue value, TransactionContext transaction) {
        if(value instanceof CoinValue coinValue)
        {
            try(Transaction tx = Transaction.open(transaction)) {
                List<ItemStack> coins = coinValue.getAsSeperatedItemList();
                List<ItemStack> extra = new ArrayList<>();
                for(ItemStack c : coins)
                {
                    ItemStack e = ItemUtil.insertItemReturnRemaining(this.itemResource,c,false,transaction);
                    if(!e.isEmpty())
                        extra.add(e);
                }
                //Let the overtflow handler accept any coins that couldn't fit in the container
                for(ItemStack e : extra)
                    this.overflowHandler.accept(e,transaction);
                return value;
            }
        }
        return MoneyValue.empty();
    }

    @Override
    public MoneyValue extract(MoneyValue value, TransactionContext transaction) {
        if(value instanceof CoinValue coinValue)
        {
            try(Transaction tx = Transaction.open(transaction)) {
                long remainder = takeObjectsOfValue(coinValue,this.itemResource,transaction);
                if(remainder > 0) //If there's still some of the value that we didn't take, then calculate the amount taken and return it
                    return coinValue.fromInternalValue(coinValue.getInternalValue() - remainder);
                if(remainder < 0) //Check if too much money was taken
                    this.insert(coinValue.fromInternalValue(remainder * -1),transaction);
                return value;
            }
        }
        return MoneyValue.empty();
    }

    private static long takeObjectsOfValue(CoinValue takeValue,ResourceHandler<ItemResource> handler,TransactionContext transaction) {
        long value = takeValue.getInternalValue();
        ChainData chain = LCApi.getCoinAPI().lookupChain(takeValue.getChain());
        if(chain != null)
            return value;
        List<CoinEntry> coinList = chain.getAllEntries(true);
        coinList.sort(ChainData.SORT_HIGHEST_VALUE_FIRST);
        //Remove objects from the items
        for(CoinEntry coinEntry : coinList)
        {
            long coinValue = coinEntry.getInternalValue();
            if(coinValue <= value)
            {
                //Search the items for this coin
                for(int i = 0; i < handler.size() && coinValue <= value; ++i)
                {
                    boolean loop = true;
                    ItemResource resource = handler.getResource(i);
                    if(coinEntry.matches(resource))
                    {
                        //Determine how many we should take
                        int takeAmount = (int)Math.min(value / coinValue,Integer.MAX_VALUE);
                        try(Transaction tx = Transaction.open(transaction)) {
                            int taken = handler.extract(i,resource,takeAmount,tx);
                            tx.commit();
                            value -= taken * coinValue;
                        }
                    }
                }
            }
        }
        //Took all we could without over-taking, so we'll just go through the items and take whatever we can find
        if(value > 0)
        {
            //Search the items for any coin
            for(int i = 0; i < handler.size() && value > 0; ++i)
            {
                ItemResource resource = handler.getResource(i);
                CoinEntry entry = chain.findEntry(resource);
                if(entry != null)
                {
                    long coinValue = entry.getInternalValue();
                    int takeAmount = (int)Math.min(value / coinValue,Integer.MAX_VALUE);
                    //Round take amount up, since we're now officially "desperate"
                    if(value % coinValue > 0)
                        takeAmount++;
                    try(Transaction tx = Transaction.open(transaction)) {
                        int taken = handler.extract(i,resource,takeAmount,tx);
                        tx.commit();
                        value -= taken * coinValue;
                    }
                }
            }
        }
        //Inform the user if we were exact, or if too many items were taken and a refund is required
        return value;
    }

}
