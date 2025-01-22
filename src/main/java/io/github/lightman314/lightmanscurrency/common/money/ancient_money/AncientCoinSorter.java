package io.github.lightman314.lightmanscurrency.common.money.ancient_money;

import io.github.lightman314.lightmanscurrency.common.items.AncientCoinItem;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public class AncientCoinSorter implements Comparator<ItemStack> {

    public static final Comparator<ItemStack> INSTANCE = new AncientCoinSorter();

    private AncientCoinSorter() {}

    @Override
    public int compare(ItemStack stack1, ItemStack stack2) {
        AncientCoinType coinType1 = AncientCoinItem.getAncientCoinType(stack1);
        AncientCoinType coinType2 = AncientCoinItem.getAncientCoinType(stack2);
        if(coinType1 == null && coinType2 == null)
            return 0;
        if(coinType2 == null)
            return -1;
        if(coinType1 == null)
            return 1;
        //If we're the same coin, sort by stack size
        if(coinType1 == coinType2)
            return Integer.compare(stack2.getCount(),stack1.getCount());
        //Otherwise, sort by type
        return Integer.compare(coinType1.ordinal(),coinType2.ordinal());
    }

}
