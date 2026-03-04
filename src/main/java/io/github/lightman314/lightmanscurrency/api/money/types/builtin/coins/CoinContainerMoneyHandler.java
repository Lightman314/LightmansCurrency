package io.github.lightman314.lightmanscurrency.api.money.types.builtin.coins;

import io.github.lightman314.lightmanscurrency.api.money.capability.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CoinContainerMoneyHandler extends MoneyHandler {

    private final IItemHandler container;
    private final Consumer<ItemStack> overflowHandler;

    public CoinContainerMoneyHandler(IItemHandler container, Consumer<ItemStack> overflowHandler)
    {
        this.container = container;
        this.overflowHandler = overflowHandler;
    }

    @Override
    public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) {
        if(insertAmount instanceof CoinValue coinValue)
        {
            // We can always insert coins into a container, even if we must use the overflow...
            if(simulation)
                return MoneyValue.empty();
            //Put coins into the container
            List<ItemStack> coins = coinValue.getAsSeperatedItemList();
            List<ItemStack> extra = new ArrayList<>();
            for(ItemStack c : coins)
            {
                ItemStack e = ItemHandlerHelper.insertItem(this.container,c,false);
                if(!e.isEmpty())
                    extra.add(e);
            }
            //Let the overflow handler accept any coins that couldn't fit in the container.
            for(ItemStack e : extra)
                this.overflowHandler.accept(e);
            return MoneyValue.empty();
        }
        return insertAmount;
    }

    @Override
    public MoneyValue extractMoney(MoneyValue extractAmount, boolean simulation)
    {
        if(extractAmount instanceof CoinValue coinValue)
        {
            if(simulation)
            {
                //Calculate the result
                MoneyValue stored = this.getStoredMoney().valueOf(extractAmount.getUniqueName());
                //If we have the amount we need to extract, tell them we'll take it all
                if(stored.containsValue(extractAmount))
                    return MoneyValue.empty();
                //If we don't have any stored, tell them we didn't take anything
                if(stored.isEmpty())
                    return extractAmount;
                //Otherwise calculate the new value
                return CoinValue.fromNumber(coinValue.getChain(),extractAmount.getCoreValue() - stored.getCoreValue());
            }
            //Can use sloppy method to take money as there's guaranteed to be an overflow handler
            long change = takeObjectsOfValue(coinValue,this.container);
            if(change > 0)
                return CoinValue.fromNumber(coinValue.getChain(),change);
            if(change < 0)
                this.insertMoney(CoinValue.fromNumber(coinValue.getChain(),change * -1), simulation);
            return MoneyValue.empty();
        }
        return extractAmount;
    }

    @Override
    public boolean isMoneyTypeValid(MoneyValue value) { return value instanceof CoinValue; }

    @Override
    protected void collectStoredMoney(MoneyView.Builder builder) {
        queryContainerContents(this.container, builder);
    }

    public static void queryContainerContents(IItemHandler container, MoneyView.Builder builder)
    {
        for(ChainData chain : CoinAPI.getApi().AllChainData())
        {
            long totalValue = 0;
            for(int i = 0; i < container.getSlots(); ++i)
            {
                ItemStack stack = container.getStackInSlot(i);
                //See how many we're allowed to take from this slot
                int takeableAmount = container.extractItem(i,stack.getCount(),true).getCount();
                totalValue += chain.getCoreValue(stack) * takeableAmount;
            }
            if(totalValue > 0)
                builder.add(CoinValue.fromNumber(chain.chain,totalValue));
        }
    }

    private static long takeObjectsOfValue(CoinValue valueToTake, IItemHandler container)
    {
        long value = valueToTake.getCoreValue();
        ChainData chainData = CoinAPI.getApi().ChainData(valueToTake.getChain());
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
                for(int i = 0; i < container.getSlots() && coinValue <= value; i++)
                {
                    boolean loop = true;
                    while(coinValue <= value && loop)
                    {
                        ItemStack stack = container.getStackInSlot(i);
                        if(coinEntry.matches(stack))
                        {
                            //Remove 1 coin
                            if(container.extractItem(i,1,false).isEmpty())
                                loop = false;
                            else
                                value -= coinValue;
                        }
                        else
                            loop = false;
                    }
                }
            }
        }
        //Took all we could without over-taking, so we'll just go through the items and take whatever we can find
        if(value > 0)
        {
            //Search the inventory for any coin
            for(int i = 0; i < container.getSlots() && value > 0; i++)
            {
                boolean loop = true;
                while(value > 0 && loop)
                {
                    ItemStack stack = container.getStackInSlot(i);
                    CoinEntry entry = chainData.findEntry(stack);
                    //Remove the coins until they would be too much money or until the stack is empty
                    if(entry != null)
                    {
                        if(container.extractItem(i,1,false).isEmpty())
                            loop = false; //End the loop if the coin could not be taken
                        else
                            value -= entry.getCoreValue();
                    }
                    else // End the loop on this slot if there is no coin here
                        loop = false;
                }
            }
        }
        //Inform the user if we were exact, or if too many items were taken and a refund is required via the getObjectsOfValue function
        return value;
    }

}
