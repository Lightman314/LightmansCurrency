package io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins;

import io.github.lightman314.lightmanscurrency.api.capability.money.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CoinContainerMoneyHandler extends MoneyHandler {

    private final Container container;
    private final Consumer<ItemStack> overflowHandler;

    public CoinContainerMoneyHandler(@Nonnull Container container, @Nonnull Consumer<ItemStack> overflowHandler)
    {
        this.container = container;
        this.overflowHandler = overflowHandler;
    }

    @Nonnull
    @Override
    public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) {
        if(insertAmount instanceof CoinValue coinValue)
        {
            Container active = simulation ? InventoryUtil.copyInventory(this.container) : this.container;
            //Put coins into the container
            List<ItemStack> coins = coinValue.getAsSeperatedItemList();
            List<ItemStack> extra = new ArrayList<>();
            for(ItemStack c : coins)
            {
                ItemStack e = InventoryUtil.TryPutItemStack(active,c);
                if(!e.isEmpty())
                    extra.add(e);
            }
            //Let the overflow handler accept any coins that couldn't fit in the container.
            //Ignore if this is a simulation
            if(!simulation)
            {
                for(ItemStack e : extra)
                    this.overflowHandler.accept(e);
            }
            return MoneyValue.empty();
        }
        return insertAmount;
    }

    @Nonnull
    @Override
    public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation)
    {
        if(extractAmount instanceof CoinValue coinValue)
        {
            //Can use sloppy method to take money as there's guaranteed to be an overflow handler
            Container active = simulation ? InventoryUtil.copyInventory(this.container) : this.container;
            long change = takeObjectsOfValue(coinValue, active);
            if(change > 0)
                return CoinValue.fromNumber(coinValue.getChain(),change);
            if(change < 0)
                this.insertMoney(CoinValue.fromNumber(coinValue.getChain(),change * -1), simulation);
            return MoneyValue.empty();
        }
        return extractAmount;
    }

    @Override
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) { return value instanceof CoinValue; }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        queryContainerContents(this.container, builder);
    }

    public static void queryContainerContents(@Nonnull Container container, @Nonnull MoneyView.Builder builder)
    {
        for(ChainData chain : CoinAPI.API.AllChainData())
        {
            long totalValue = 0;
            for(int i = 0; i < container.getContainerSize(); ++i)
            {
                ItemStack stack = container.getItem(i);
                totalValue += chain.getCoreValue(stack) * stack.getCount();
            }
            if(totalValue > 0)
                builder.add(CoinValue.fromNumber(chain.chain, totalValue));
        }
    }

    private static long takeObjectsOfValue(@Nonnull CoinValue valueToTake, @Nonnull Container container)
    {
        long value = valueToTake.getCoreValue();
        ChainData chainData = CoinAPI.API.ChainData(valueToTake.getChain());
        if(chainData == null)
            return value;
        List<CoinEntry> coinList = chainData.getAllEntries(true);
        coinList.sort(ChainData.SORT_HIGHEST_VALUE_FIRST);
        //Remove objects from the inventory.
        for(CoinEntry coinEntry : coinList)
        {
            long coinValue = coinEntry.getCoreValue();
            if(coinValue <= value)
            {
                //Search the inventory for this coin
                for(int i = 0; i < container.getContainerSize() && coinValue <= value; i++)
                {
                    ItemStack itemStack = container.getItem(i);
                    if(coinEntry.matches(itemStack))
                    {
                        //Remove the coins until they would be too much money or until the stack is empty.
                        while(coinValue <= value && !itemStack.isEmpty())
                        {
                            value -= coinValue;
                            itemStack.shrink(1);
                            if(itemStack.isEmpty())
                                container.setItem(i, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
        //Took all we could without over-taking, so we'll just go through the coinList backwards until we have what we need.
        if(value > 0)
        {
            coinList.sort(ChainData.SORT_LOWEST_VALUE_FIRST);
            for(CoinEntry coinEntry : coinList)
            {
                long coinValue = coinEntry.getCoreValue();
                //Search the inventory for this coin
                for(int i = 0; i < container.getContainerSize() && value > 0; i++)
                {
                    ItemStack itemStack = container.getItem(i);
                    if(coinEntry.matches(itemStack))
                    {
                        //Remove the coins until they would be too much money or until the stack is empty.
                        while(value > 0 && !itemStack.isEmpty())
                        {
                            value -= coinValue;
                            itemStack.shrink(1);
                            if(itemStack.isEmpty())
                                container.setItem(i, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
        //Inform the user if we were exact, or if too many items were taken and a refund is required via the getObjectsOfValue function
        return value;
    }

}
