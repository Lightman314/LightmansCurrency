package io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.data;

import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MutableCoreChain {

    @Nullable
    public Item rootItem;
    public final List<MutableCoinEntry> entries = new ArrayList<>();

    public MutableCoreChain() { }

    public void copyFrom(List<CoinEntry> entry)
    {
        this.rootItem = null;
        this.entries.clear();
        if(!entry.isEmpty())
        {
            this.rootItem = entry.getFirst().getCoin();
            for(int i = 1; i < entry.size(); ++i)
                this.entries.add(new MutableCoinEntry(entry.get(i)));
        }
    }

    public void addItem(Item item) {
        if(this.rootItem == null)
            this.rootItem = item;
        else
            this.entries.add(new MutableCoinEntry(item));
    }

}
