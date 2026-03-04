package io.github.lightman314.lightmanscurrency.common.money.ancient_money.handlers;

import io.github.lightman314.lightmanscurrency.api.money.capability.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.items.AncientCoinItem;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.AncientMoneyValue;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AncientContainerMoneyHandler extends MoneyHandler {

    private final IItemHandler container;
    private final Consumer<ItemStack> overflowHandler;

    public AncientContainerMoneyHandler(IItemHandler container, Consumer<ItemStack> overflowHandler)
    {
        this.container = container;
        this.overflowHandler = overflowHandler;
    }

    @Override
    public MoneyValue insertMoney(MoneyValue insertAmount, boolean simulation) {
        if(insertAmount instanceof AncientMoneyValue value)
        {
            //If this is a simulation, simply acknowledge that all coins will be inserted into the container properly
            if(simulation)
                return MoneyValue.empty();
            //Put coins into the container
            List<ItemStack> coins = value.getAsSeperatedItemList();
            List<ItemStack> extra = new ArrayList<>();
            for(ItemStack c : coins)
            {
                ItemStack e = ItemHandlerHelper.insertItem(this.container,c,simulation);
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
        if(extractAmount instanceof AncientMoneyValue value)
        {
            long leftToTake = value.count;
            for(int i = 0; i < this.container.getSlots(); ++i)
            {
                ItemStack item = this.container.getStackInSlot(i);
                if(simulation)
                    item = item.copy();
                AncientCoinType type = AncientCoinItem.getAncientCoinType(item);
                if(type != null && type == value.type)
                {
                    //Take items
                    if(leftToTake > 0)
                    {
                        ItemStack result = this.container.extractItem(i,(int)Math.min(leftToTake,Integer.MAX_VALUE),simulation);
                        //ItemStack#split will only take *at most* the current stack count
                        leftToTake -= result.getCount();
                    }
                }
            }
            //If we took everything, return empty
            if(leftToTake <= 0)
                return MoneyValue.empty();
            //Otherwise return the amount not taken
            return AncientMoneyValue.of(value.type,leftToTake);
        }
        return extractAmount;
    }

    @Override
    public boolean isMoneyTypeValid(MoneyValue value) { return value instanceof AncientMoneyValue; }

    @Override
    protected void collectStoredMoney(MoneyView.Builder builder) {
        queryContainerContents(this.container, builder);
    }

    public static void queryContainerContents(IItemHandler container, MoneyView.Builder builder)
    {
        Map<AncientCoinType,Integer> map = new HashMap<>();
        for(int i = 0; i < container.getSlots(); ++i)
        {
            ItemStack item = container.getStackInSlot(i);
            AncientCoinType type = AncientCoinItem.getAncientCoinType(item);
            if(type != null)
            {
                int takeableCount = container.extractItem(i,Integer.MAX_VALUE,true).getCount();
                map.put(type,map.getOrDefault(type,0) + takeableCount);
            }
        }
        map.forEach((type,count) -> builder.add(AncientMoneyValue.of(type,count)));
    }

}