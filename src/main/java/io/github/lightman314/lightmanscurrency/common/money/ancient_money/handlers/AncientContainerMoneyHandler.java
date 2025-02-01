package io.github.lightman314.lightmanscurrency.common.money.ancient_money.handlers;

import io.github.lightman314.lightmanscurrency.api.capability.money.MoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.items.AncientCoinItem;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.money.ancient_money.AncientMoneyValue;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AncientContainerMoneyHandler extends MoneyHandler {

    private final Container container;
    private final Consumer<ItemStack> overflowHandler;

    public AncientContainerMoneyHandler(@Nonnull Container container, @Nonnull Consumer<ItemStack> overflowHandler)
    {
        this.container = container;
        this.overflowHandler = overflowHandler;
    }

    @Nonnull
    @Override
    public MoneyValue insertMoney(@Nonnull MoneyValue insertAmount, boolean simulation) {
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
                ItemStack e = InventoryUtil.TryPutItemStack(this.container,c);
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

    @Nonnull
    @Override
    public MoneyValue extractMoney(@Nonnull MoneyValue extractAmount, boolean simulation)
    {
        if(extractAmount instanceof AncientMoneyValue value && this.getStoredMoney().containsValue(extractAmount))
        {
            long leftToTake = value.count;
            //Cannot take more than an integers worth of ancient coins, as it's not possible for that amount to be in a container
            if(leftToTake > Integer.MAX_VALUE)
                return extractAmount;
            for(int i = 0; i < this.container.getContainerSize(); ++i)
            {
                ItemStack item = this.container.getItem(i);
                if(simulation)
                    item = item.copy();
                AncientCoinType type = AncientCoinItem.getAncientCoinType(item);
                if(type != null && type == value.type)
                {
                    //Take items
                    if(leftToTake > 0)
                    {
                        //ItemStack#split will only take *at most* the current stack count
                        leftToTake -= item.split((int)leftToTake).getCount();
                        if(!simulation)
                        {
                            if(item.isEmpty())
                                this.container.setItem(i,ItemStack.EMPTY);
                            else
                                this.container.setItem(i,item);
                        }
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
    public boolean isMoneyTypeValid(@Nonnull MoneyValue value) { return value instanceof AncientMoneyValue; }

    @Override
    protected void collectStoredMoney(@Nonnull MoneyView.Builder builder) {
        queryContainerContents(this.container, builder);
    }

    public static void queryContainerContents(@Nonnull Container container, @Nonnull MoneyView.Builder builder)
    {
        Map<AncientCoinType,Integer> map = new HashMap<>();
        for(int i = 0; i < container.getContainerSize(); ++i)
        {
            ItemStack item = container.getItem(i);
            AncientCoinType type = AncientCoinItem.getAncientCoinType(item);
            if(type != null)
            {
                map.put(type, map.getOrDefault(type,0) + item.getCount());
            }
        }
        map.forEach((type,count) -> builder.add(AncientMoneyValue.of(type,count)));
    }

}